package org.social.integrations.birdview

import org.social.integrations.birdview.model.BVTaskGroup
import org.social.integrations.birdview.source.jira.JiraClient
import javax.inject.Named
import kotlin.math.min

@Named
class GroupDescriber(val jiraClient: JiraClient) {
    private val jiraKeyPattern = "\\w+-\\d+".toRegex()
    private val urlPattern = "http[s]*://.*".toRegex()

    fun describe(groups: List<BVTaskGroup>) {
        val jiraGroups = mutableMapOf<String, MutableList<BVTaskGroup>>()
        for(group in groups) {
            val sortedTerms = group.getTerms()
                    .toSet()
                    .filter { !it.contains(" ") && !it.matches(urlPattern) }
                    .sortedByDescending { group.getTermFrequency(it) }
            val shortList = sortedTerms.subList(0, min(3, sortedTerms.size))
            group.title = shortList.map { it.capitalize() }.joinToString(" ")

            val maybeJIRAKey = shortList.find { it.matches(jiraKeyPattern) }
            if (maybeJIRAKey != null) {
                jiraGroups
                        .computeIfAbsent(maybeJIRAKey, { mutableListOf<BVTaskGroup>() })
                        .add(group)
            }
        }

        // Rewrite titles with Jira issues summaries
        if(!jiraGroups.isEmpty()) {
            val issues = jiraClient.loadIssues(jiraGroups.keys.toList())
            for (issue in issues) {
                jiraGroups[issue.key]?.forEach { it.title = issue.fields.summary }
            }
        }
    }
}