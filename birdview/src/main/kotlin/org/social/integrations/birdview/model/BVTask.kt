package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms

class BVTask (
        val id: String,
        val title: String,
        val updated: String,
        val created: String,
        val httpUrl: String,
        val priority: Int
) {
    private val elevatedTerms = ElevatedTerms()

    fun getTerms():List<BVTerm> = elevatedTerms.getTerms()

    fun addTerms(extractTerms: List<BVTerm>) {
        elevatedTerms.addTerms(extractTerms)
    }

    fun updateTerms(otherElevatedTerms: ElevatedTerms) {
        elevatedTerms.updateTerms(otherElevatedTerms)
    }
}

data class BVTerm (
    val term: String,
    val weight: Double
)