package org.social.integrations.birdview.model

import org.social.integrations.birdview.analysis.Document
import org.social.integrations.birdview.analysis.tokenize.ElevatedTerms
import java.util.*

class BVTask (
        val id: String,
        val title: String,
        val updated: Date,
        val created: Date,
        val httpUrl: String,
        val priority: Int,
        val description: String? = ""
) : Document {
    private val elevatedTerms = ElevatedTerms()

    fun getBVTerms():List<BVTerm> = elevatedTerms.getTerms()

    fun addTerms(extractTerms: List<BVTerm>) {
        elevatedTerms.addTerms(extractTerms)
    }

    fun updateTerms(otherElevatedTerms: ElevatedTerms) {
        elevatedTerms.updateTerms(otherElevatedTerms)
    }

    override fun getTerms(): List<String> = getBVTerms().map { it.term }

    override fun getTermFrequency(term: String): Int = getTerms().count { it == term }
}

data class BVTerm (
    val term: String,
    val weight: Double
)