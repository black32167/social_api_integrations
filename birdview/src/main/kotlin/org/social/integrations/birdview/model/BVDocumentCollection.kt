package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.DocumentGroupId
import java.util.*
import kotlin.Comparator

class BVDocumentCollection {
    val documents = sortedSetOf<BVDocument> (Comparator<BVDocument> { t1, t2 -> t2.updated.compareTo(t1.updated) })
    var title:String? = null
    val groupIds = mutableSetOf<DocumentGroupId>()

    fun addDocument(task:BVDocument) {
        documents.add(task)
        groupIds.addAll(task.groupIds)
    }

    fun getLastUpdated(): Date =
            documents.first()?.updated ?: documents.first().created
}