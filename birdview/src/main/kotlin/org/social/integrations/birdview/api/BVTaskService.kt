package org.social.integrations.birdview.api

import org.social.integrations.birdview.analysis.TfIdfCalclulator
import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTaskGroup
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.github.GithubTaskService
import org.social.integrations.birdview.source.jira.JiraTaskService
import org.social.integrations.birdview.source.trello.TrelloTaskService
import org.social.integrations.birdview.utils.BVConcurrentUtils
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named
import kotlin.math.sqrt

@Named
class BVTaskService(
        private var jira: JiraTaskService,
        private val trello: TrelloTaskService,
        private val github: GithubTaskService
)  {
    private val executor = Executors.newFixedThreadPool(3, BVConcurrentUtils.getDaemonThreadFactory())
    private val proximityMergingThreshold = 0.1
    private val tfIdfCalclulator = TfIdfCalclulator()

    fun getTaskGroups(status: String, grouping: Boolean): List<BVTaskGroup> {
        val groups = mutableListOf<BVTaskGroup>()
        val tasks = mutableListOf<BVTask>()
        val start = System.currentTimeMillis()
        val sources = listOf<BVTaskSource>(jira, trello, github)

        val futures = sources.map { source ->
            executor.submit(Callable<List<BVTask>> { source.getTasks(status) })
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

        elevateTerms(tasks)

        // Sort tasks by update time
        tasks.sortBy { it.updated }

        for(task in tasks) {
            tfIdfCalclulator.addDoc(task)
        }

        for(task in tasks) {
            if(!(grouping && addToGroup(groups, task))) {
                groups.add(newGroup(task))
            }
        }

        groups.sortByDescending { it.getLastUpdated() }

        return groups;
    }

    private fun elevateTerms(tasks: List<BVTask>) {
        val elevatedTerms = ElevatedTerms()
        tasks.forEach {
            elevatedTerms.addTerms (it.getBVTerms())
        }
        tasks.forEach {task->
            task.updateTerms(elevatedTerms)
        }

        // elevatedTerms.getTerms().sortedBy { it.term }.forEach { println("${it.term}:${it.weight}") }

    }

    private fun newGroup(task: BVTask): BVTaskGroup =
        BVTaskGroup().also {it.addTask(task) }

    // NOTE: side-effect: sorts groups
    private fun addToGroup(groups: MutableList<BVTaskGroup>, task: BVTask): Boolean {
        var candidateGroup:BVTaskGroup? = null
        var candidateProximity = 0.0

        val maxTimeDistanceMs = 1000*60*60*24
        for(i in groups.size-1 downTo 0) {
            val group = groups[i]
            val timeDistance = task.updated.time - group.getLastUpdated().time
            if (timeDistance > maxTimeDistanceMs) {
                break
            }
            val proximity = calculateSimilarity(group, task)
            if(proximity > candidateProximity) {
                candidateGroup = group
                candidateProximity = proximity
            }
        }

        if(candidateGroup != null && candidateProximity > proximityMergingThreshold) {
            candidateGroup.addTask(task)

            //Resort groups
            groups.sortBy { it.getLastUpdated() }

            return true
        }

        return false
    }

    private fun calculateSimilarity(group: BVTaskGroup, task: BVTask): Double {
        val groupVecSize = sqrt(group.getTerms().map { tfIdfCalclulator.calculate(it, group) }.sum())
        val taskVecSize = sqrt(task.getTerms().map { tfIdfCalclulator.calculate(it, task) }.sum())
        val groupTaskProdVecSize = sqrt(group.getTerms().map { gTerm->
            tfIdfCalclulator.calculate(gTerm, group) * tfIdfCalclulator.calculate(gTerm, task);
        }. sum())

        val cos = groupTaskProdVecSize/(groupVecSize*taskVecSize)
        if(cos > 0.01) {
            println("[${task.title}-${group.getTitle()}]: $cos")
        }
        return cos
//        val termsDistance = task.getBVTerms().map { bvTerm ->
//            val groupTerm = group.groupTerms.findTerm(bvTerm.term)
//            groupTerm
//                    ?.let {
//                        Math.max(bvTerm.weight, it.weight)
//                    }
//                    ?: 0.0
//        }.sumByDouble { it }
//
//        //val logTermDistance = Math.log(termsDistance)
//        val proximity = termsDistance
//       println("[${task.title}-${group.getTitle()}]: $termsDistance")
//        return proximity
    }
}