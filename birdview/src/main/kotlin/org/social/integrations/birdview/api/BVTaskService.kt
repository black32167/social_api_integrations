package org.social.integrations.birdview.api

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

@Named
class BVTaskService(
        private var jira: JiraTaskService,
        private val trello: TrelloTaskService,
        private val github: GithubTaskService
)  {
    private val executor = Executors.newFixedThreadPool(3, BVConcurrentUtils.getDaemonThreadFactory())
    private val proximityMergingThreshold = 2.0

    fun getTaskGroups(status: String): List<BVTaskGroup> {
        val groups = mutableListOf<BVTaskGroup>()
        val tasks = mutableListOf<BVTask>()
        val start = System.currentTimeMillis()
        val sources = listOf<BVTaskSource>(jira, trello, github)

        val futures = sources.map { source ->
            executor.submit(object : Callable<List<BVTask>> {
                override fun call() = source.getTasks(status)
            })
        }

        for (future in futures) {
            tasks.addAll(future.get())
        }

        val end = System.currentTimeMillis();
        println("Request took ${end-start} ms.")

        for(task in tasks) {
            if(!addToGroup(groups, task)) {
                groups.add(newGroup(task))
            }
        }

        groups.sortByDescending { it.getLastUpdated() }

        return groups;
    }

    private fun newGroup(task: BVTask): BVTaskGroup =
        BVTaskGroup().also {it.addTask(task) }

    private fun addToGroup(groups: MutableList<BVTaskGroup>, task: BVTask): Boolean {
        var candidateGroup:BVTaskGroup? = null
        var candidateProximity = 0.0
        for (group in groups) {
            val proximity = calculateProximity(group, task)
            if(proximity > candidateProximity) {
                candidateGroup = group
                candidateProximity = proximity
            }
        }

        if(candidateGroup != null && candidateProximity > proximityMergingThreshold) {
            candidateGroup.addTask(task)
            return true
        }

        return false
    }

    private fun calculateProximity(group: BVTaskGroup, task: BVTask): Double =
        task.terms.map {
            if(group.groupTerms.keys.contains(it.term)) it.weight else 0.0
        }.sumByDouble { it }
}