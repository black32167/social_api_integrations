package org.social.integrations.birdview.analysis.tokenize

import org.social.integrations.birdview.model.BVTerm
import javax.inject.Named

@Named
class TextTokenizer(val stopWordsService: StopWordsService) {
    companion object {
        val TOKEN_DELIMETERS = "[ '\\-(),.:\"/]".toRegex()
        const val HEADING_TOKEN_WEIGHT = 6.0
        const val JIRA_KEY_WEIGHT = 3.0
        const val SEQUENTIAL_WEIGHT = 2.0
        const val DEFAULT_WEIGHT = 1.0
    }
    fun tokenize(text:String) : List<BVTerm> {
        val termsList = mutableListOf<BVTerm>()

        // Leading terms extraction:
        val leadingTerms = split(text.substringBefore(':', ""))
        leadingTerms.forEach { token->
            termsList.add( BVTerm(token, HEADING_TOKEN_WEIGHT))
        }

        // Create initial list of individual tokens
        val filteredTokens = split(text.substringAfter(":"))
        filteredTokens.forEach { token->
            val stemmed = stem(token)
            val bvTerm = when {
                token.matches("\\w+-\\d+".toRegex()) -> BVTerm(stemmed, JIRA_KEY_WEIGHT)
                else -> BVTerm(stemmed, DEFAULT_WEIGHT)
            }
            termsList.add(bvTerm)
        }


        // Generate sequential tokens
        val sequentialTokens = filteredTokens.windowed(2, 1).map { window->
            window.joinToString (" ") { it }
        }
        sequentialTokens.forEach {token->
            termsList.add(BVTerm(token, SEQUENTIAL_WEIGHT))
        }

        return termsList.toList()
    }

    private fun stem(token: String): String
        = if(token.endsWith("s"))  token.substringBeforeLast("s")
        else token


    private fun split(text: String)
        = text.split(TOKEN_DELIMETERS)
            .map { it.trim().toLowerCase() }
            .filter { !it.isBlank() && !stopWordsService.isStopWord(it) }
            .toList()
}