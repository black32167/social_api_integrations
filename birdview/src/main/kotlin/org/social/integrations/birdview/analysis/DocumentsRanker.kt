package org.social.integrations.birdview.analysis

interface DocumentsRanker {
    fun rank(doc: BVDocument, corpus: List<BVDocument>)
}