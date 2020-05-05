package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.Document
import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms
import java.util.*
import kotlin.Comparator
import kotlin.math.min

class BVTaskGroup : Document {
    val tasks = sortedSetOf<BVTask> (Comparator<BVTask> { t1, t2 -> t2.updated.compareTo(t1.updated) })
    val groupTerms = ElevatedTerms()

    fun addTask(task:BVTask) {
        tasks.add(task)
        groupTerms.addTerms(task.getBVTerms())
    }

    fun getTitle(): String {
        val sortedTerms = tasks.flatMap { it.getTerms() }.toSet().sortedByDescending { getTermFrequency(it) }
        return sortedTerms.subList(0, min(3, sortedTerms.size)).joinToString(" ")
    }

    fun describe(task: BVTask): String
        = "${task.updated} - ${task.title} : ${task.httpUrl}"

    fun getLastUpdated(): Date =
            tasks.first()?.updated ?: tasks.first().created

    override fun getTerms(): List<String> = tasks.flatMap { it.getTerms() }

    override fun getTermFrequency(term: String): Int = getTerms().count { it == term }
}