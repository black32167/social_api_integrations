package org.social.integrations.birdview.model

class BVTask (
        val id: String,
        val title: String,
        val updated: String,
        val httpUrl: String,
        val terms: List<BVTerm>
)

class BVTerm (
    val term: String,
    val weight: Double
)