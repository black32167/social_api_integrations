package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.Document
import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms
import java.util.*
import kotlin.Comparator

class BVTaskGroup : Document {
    val tasks = sortedSetOf<BVTask> (Comparator<BVTask> { t1, t2 -> t2.updated.compareTo(t1.updated) })
    val groupTerms = ElevatedTerms()
    var title:String? = null

    fun addTask(task:BVTask) {
        tasks.add(task)
        groupTerms.addTerms(task.getBVTerms())
    }

    fun getLastUpdated(): Date =
            tasks.first()?.updated ?: tasks.first().created

    override fun getTerms(): List<String> = tasks.flatMap { it.getTerms() }

    override fun getTermFrequency(term: String): Int = getTerms().count { it == term }
}