package org.social.integrations.birdview.utils

object BVFilters {
    private val JIRA_KEY = "\\w+-\\d+".toRegex()
    private val UUID1 = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()
    private val ALPHANUMERIC_ID = "\\b[0-9a-fA-Z]*\\b".toRegex()

    fun filterIds(terms: List<String>): List<String>
            = terms.filter { it.matches(JIRA_KEY) or it.matches(UUID1) or it.matches(ALPHANUMERIC_ID)}
}
