package org.social.integrations.birdview.analysis

import java.util.*

open class BVDocument (
        val ids: Set<BVDocumentId>,
        var title: String? = null,
        val body: String = "",
        val updated: Date? = null,
        val created: Date? = null,
        val httpUrl: String? = null,
        val subDocuments: MutableList<BVDocument> = mutableListOf(),
        val groupIds: Set<BVDocumentId> = emptySet(),
        val refsIds: List<String> = emptyList(),
        val status: String? = null
) {
    val inferredIds: MutableSet<BVDocumentId> = mutableSetOf<BVDocumentId>()
            .apply { addAll(ids) }

    fun addDocument(task:BVDocument) {
        subDocuments.add(task)
        inferredIds.addAll(task.groupIds)
    }

    fun getLastUpdated(): Date? =
            subDocuments.mapNotNull { it.updated }.min()
}

data class BVDocumentId(
        val id:String,
        val type:String,
        val sourceName:String)