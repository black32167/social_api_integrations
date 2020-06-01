package org.social.integrations.birdview.api

import org.social.integrations.birdview.GroupDescriber
import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.BVDocumentId
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

    fun getTaskGroups(request: TasksRequest): List<BVDocument> {
        val groupingIdsMap = mutableMapOf<BVDocumentId, List<BVDocument>>()
        val docs = mutableListOf<BVDocument>()
        val start = System.currentTimeMillis()
        val sources = listOf<BVTaskSource>(jira, trello, github)

        val futures = sources.map { source ->
            executor.submit(Callable<List<BVDocument>> { source.getTasks(request) })
        }
        for (future in futures) {
            try {
                docs.addAll(future.get())
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }

        linkDocs(docs)

        // Collect all groupIds
        val groupedDocumentsMap = docs.fold(mutableMapOf<BVDocumentId, MutableList<BVDocument>>()) { acc, doc ->
            doc.groupIds.forEach { groupId ->
                acc.computeIfAbsent(groupId) { mutableListOf() }.add(doc)
            }
            acc
        }

        val groupedDocuments:List<BVDocument> = groupedDocumentsMap
                .map { (groupDocId, collection) -> newGroupDoc(groupDocId, collection) }
                .sortedByDescending { it.getLastUpdated() }

        groupDescriber.describe(groupedDocuments)

        return groupedDocuments
    }

    private fun newGroupDoc(groupDocId: BVDocumentId, collection: List<BVDocument>):BVDocument =
            BVDocument(ids = listOf(groupDocId), subDocuments = collection.toMutableList())

    private fun linkDocs(docs: MutableList<BVDocument>) {
        val groupId2Group = docs
                .flatMap { collection -> collection.ids.map { it to collection } }
                .groupBy ({ entry -> entry.first.id }, { entry -> entry.second })

        val collectionsIterator = docs.iterator()
        while (collectionsIterator.hasNext()) {
            collectionsIterator.next()
                    .also { doc:BVDocument ->
                        val targetDocs:List<BVDocument> = doc.refsIds
                                .flatMap { refId -> (groupId2Group[refId] ?: emptyList<BVDocument>()) }
                        if (!targetDocs.isEmpty()) {
                            targetDocs.forEach{ it.subDocuments.add(doc) }
                            collectionsIterator.remove()
                        }
                    }
        }
    }

}