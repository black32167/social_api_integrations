package org.social.integrations.birdview.analysis.tokenize

import org.social.integrations.birdview.model.BVTerm

class ElevatedTerms {
    protected val termsMap = mutableMapOf<String, BVTerm>()

    fun addTerms(terms: Collection<BVTerm>) {
        terms.forEach {bvTerm->
            val existingTerm = termsMap[bvTerm.term]
            if(existingTerm == null || existingTerm.weight < bvTerm.weight) {
                termsMap[bvTerm.term] = bvTerm
            }
        }
    }

    fun getTerms():List<BVTerm> = termsMap.values.toList()

    fun findTerm(term: String): BVTerm?
        = termsMap[term]

    fun updateTerms(otherElevatedTerms: ElevatedTerms) {
        termsMap.keys.forEach {token->
            otherElevatedTerms.findTerm(token)?.also { externalTerm->
                val existingTerm = termsMap[token]!!
                if(externalTerm.weight > existingTerm.weight) {
                    termsMap[token] = externalTerm
                }
            }
        }
    }
}