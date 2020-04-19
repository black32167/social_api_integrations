package org.social.integrations.birdview.model

class BVTaskGroup {
    val tasks = sortedSetOf<BVTask> (object:Comparator<BVTask> {
        override fun compare(t1:BVTask, t2: BVTask) = t1.updated.compareTo(t2.updated)
    })
    val groupTerms = mutableMapOf<String, BVTerm>()

    fun addTask(task:BVTask) {
        tasks.add(task)
        task.terms.forEach { term->
            val existingTask = groupTerms[term.term]
            if (existingTask == null || existingTask.weight < term.weight) {
                groupTerms[term.term] = term
            }
        }
    }

    fun getTitle(): String =
            tasks.last()?.title?.substringBefore(':') ?: "---"

    fun getLastUpdated(): String =
            tasks.last()?.updated ?: ""
}