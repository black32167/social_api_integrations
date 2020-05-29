package org.social.integrations.birdview.api

import org.social.integrations.birdview.GroupDescriber
import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.model.BVDocumentCollection
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.github.GithubTaskService
import org.social.integrations.birdview.source.jira.JiraTaskService
import org.social.integrations.birdview.source.trello.TrelloTaskService
import org.social.integrations.birdview.utils.BVConcurrentUtils
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named

@Named
class BVTaskService(
        private val groupDescriber: GroupDescriber,
        private var jira: JiraTaskService,
        private val trello: TrelloTaskService,
        private val github: GithubTaskService
)  {
    private val executor = Executors.newFixedThreadPool(3, BVConcurrentUtils.getDaemonThreadFactory())
   // private val tfIdfCalclulator = TfIdfCalclulator()

    fun getTaskGroups(request: TasksRequest): List<BVDocumentCollection> {
        val groups = mutableListOf<BVDocumentCollection>()
        val tasks = mutableListOf<BVDocument>()
        val start = System.currentTimeMillis()
        val sources = listOf<BVTaskSource>(jira, trello, github)

        val futures = sources.map { source ->
            executor.submit(Callable<List<BVDocument>> { source.getTasks(request) })
        }
        for (future in futures) {
            try {
                tasks.addAll(future.get())
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }

        val end = System.currentTimeMillis();
        //println("Request took ${end-start} ms.")

        // Sort tasks by update time
        tasks.sortBy { it.updated }

        for(task in tasks) {
            if(!(request.grouping && addToGroup(groups, task, request.groupingThreshold))) {
                groups.add(newGroup(task))
            }
        }

        groups.sortByDescending { it.getLastUpdated() }
        linkSingulars(groups)
        mergeSingulars(groups)
        groupDescriber.describe(groups)

        return groups;
    }

    private fun linkSingulars(groups: MutableList<BVDocumentCollection>) {
        val groupId2Group = groups
                .flatMap { collection -> collection.groupIds.map { it to collection } }
                .groupBy ({ entry -> entry.first.id }, { entry -> entry.second })
        val ids2Collection = groups
                .flatMap { collection -> collection.documents.map { it to collection } }
                .groupBy ({ entry -> entry.first.id }, { entry -> entry.second })

        groups
                .filter { it.documents.size == 1 }
                .flatMap { it.documents }
                .forEach { doc:BVDocument ->
                    doc.refsIds.forEach { refId ->
                        groupId2Group[refId]?.forEach{
                            collection -> collection.documents.add(doc)
                        }
                        ids2Collection[refId]?.forEach{
                            collection -> collection.documents.add(doc)
                        }
                    }
                }
    }

    private fun mergeSingulars(groups: MutableList<BVDocumentCollection>) {
        // Merge orphaned groups together
        val it = groups.iterator()
        val defaultGroup = BVDocumentCollection().apply { title = "--- Others ----" }
        while (it.hasNext()) {
            val g = it.next()
            if(g.documents.size < 2) {
                defaultGroup.documents.addAll(g.documents)
                it.remove()
            }
        }

        groups.add(defaultGroup)
    }

    private fun newGroup(task: BVDocument): BVDocumentCollection =
        BVDocumentCollection().also {it.addDocument(task) }

    // NOTE: side-effect: sorts groups
    private fun addToGroup(groups: MutableList<BVDocumentCollection>, task: BVDocument, proximityMergingThreshold: Double): Boolean {
        var candidateGroup:BVDocumentCollection? = null
        var candidateProximity = 0.0

        val maxTimeDistanceMs = 1000*60*60*24
        for(i in groups.size-1 downTo 0) {
            val group = groups[i]
//            val timeDistance = task.updated.time - group.getLastUpdated().time
//            if (timeDistance > maxTimeDistanceMs) {
//                break
//            }
            val proximity = calculateSimilarity(group, task)
            if(proximity > candidateProximity) {
                candidateGroup = group
                candidateProximity = proximity
            }
        }

        if(candidateGroup != null && candidateProximity > proximityMergingThreshold) {
            candidateGroup.addDocument(task)

            //Resort groups
            groups.sortBy { it.getLastUpdated() }

            return true
        }

        return false
    }

    private fun calculateSimilarity(group: BVDocumentCollection, task: BVDocument): Double =
        if (task.groupIds.any { group.groupIds.contains(it) }) 1.0 else 0.0
}