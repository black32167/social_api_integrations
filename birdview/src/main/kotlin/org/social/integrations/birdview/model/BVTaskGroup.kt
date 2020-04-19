package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms

class BVTaskGroup {
    val tasks = sortedSetOf<BVTask> (object:Comparator<BVTask> {
        override fun compare(t1:BVTask, t2: BVTask) = t2.updated.compareTo(t1.updated)
    })
    val groupTerms = ElevatedTerms()

    fun addTask(task:BVTask) {
        tasks.add(task)
        groupTerms.addTerms(task.getTerms())
    }

    fun getTitle(): String =
            tasks.last()?.title?.substringBefore(':') ?: "---"

    fun getLastUpdated(): String =
            tasks.first()?.updated ?: ""
}