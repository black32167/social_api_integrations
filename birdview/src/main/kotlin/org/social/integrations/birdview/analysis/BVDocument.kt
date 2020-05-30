package org.social.integrations.birdview.analysis

import java.util.*

open class BVDocument (
        val sourceName: String,
        val ids: List<String>,
        val title: String,
        val body: String,
        val updated: Date,
        val created: Date,
        val httpUrl: String,
        val groupIds: List<DocumentGroupId>,
        val refsIds: List<String>,
        val status: String? = null
)

data class DocumentGroupId(
        val id:String,
        val type:String,
        val sourceName:String)