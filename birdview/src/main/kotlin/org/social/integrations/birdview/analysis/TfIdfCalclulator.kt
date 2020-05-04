package org.social.integrations.birdview.analysis

import java.util.concurrent.atomic.AtomicInteger

import kotlin.math.log10

class TfIdfCalclulator {
    private val termsDocsCounts = mutableMapOf<String, AtomicInteger>()
    private var docsCount = 0

    fun addDoc(doc:Document) {
        val termSet = mutableSetOf<String>()
        doc.getTerms().forEach { term ->
            if(termSet.add(term)) { // Count term only once per document
                termsDocsCounts.computeIfAbsent(term) {
                    AtomicInteger(0)
                }.incrementAndGet()
            }
        }
        docsCount++
    }

    fun idf(term:String):Double = termsDocsCounts[term]?.let {
        termCount -> 1 + log10(docsCount.toDouble()/termCount.get())
    } ?: 1.0

    fun calculate(term:String, doc:Document) = doc.getTermFrequency(term) * idf(term)
}