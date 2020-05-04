package org.social.integrations.birdview.analysis

interface Document {
    fun getTerms():List<String>
    fun getTermFrequency(term:String):Int
}