package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.Document
import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms
import java.util.*
import kotlin.Comparator

class BVTaskGroup : Document {
    val tasks = sortedSetOf<BVTask> (Comparator<BVTask> { t1, t2 -> t2.updated.compareTo(t1.updated) })
    val groupTerms = ElevatedTerms()

    fun addTask(task:BVTask) {
        tasks.add(task)
        groupTerms.addTerms(task.getBVTerms())
    }

    fun getTitle(): String {
        val priorityGroups:Map<Int, List<BVTask>> = tasks.groupBy { it.priority }.toSortedMap (Comparator<Int> { p1, p2 -> p1.compareTo(p2) })
        val titleTask:BVTask? = priorityGroups.values.firstOrNull()?.sortedByDescending { it.updated }?.last()

        return titleTask?.title?.substringBefore(':') ?: "---"
        //return titleTask?.let { describe(it) } ?: "---"
    }

    fun describe(task: BVTask): String
        = "${task.updated} - ${task.title} : ${task.httpUrl}"

    fun getLastUpdated(): Date =
            tasks.first()?.updated ?: tasks.first().created

    override fun getTerms(): List<String> = tasks.flatMap { it.getTerms() }

    override fun getTermFrequency(term: String): Int = getTerms().count { it == term }
}