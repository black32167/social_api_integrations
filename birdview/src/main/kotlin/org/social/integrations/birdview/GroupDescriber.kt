package org.social.integrations.birdview

import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.model.BVDocumentCollection
import org.social.integrations.birdview.source.jira.JiraClientProvider
import org.social.integrations.birdview.source.jira.JiraTaskService
import javax.inject.Named

@Named
class GroupDescriber(
        private val jiraClientProvider: JiraClientProvider,
        private var sourcesConfigProvider: BVSourcesConfigProvider
) {
    private val jiraKeyPattern = "\\w+-\\d+".toRegex()
    private val urlPattern = "http[s]*://.*".toRegex()

    fun describe(groups: List<BVDocumentCollection>) {
        val jiraGroups = mutableMapOf<String, MutableList<BVDocumentCollection>>()
        for(group in groups) {
            val maybeJIRAKey = group.groupIds
                    .find { it.type == JiraTaskService.JIRA_KEY_TYPE }
                    ?.id

            if (maybeJIRAKey != null) {
                jiraGroups
                        .computeIfAbsent(maybeJIRAKey, { mutableListOf<BVDocumentCollection>() })
                        .add(group)
            }
        }

        // Rewrite titles with Jira issues summaries
        if(!jiraGroups.isEmpty()) {
            // TODO: parallelize
            sourcesConfigProvider.getConfigsOfType(BVJiraConfig::class.java)
                    .map { jiraConfig->jiraClientProvider.getJiraClient(jiraConfig) }
                    .flatMap { jiraClient->jiraClient.loadIssues(jiraGroups.keys.toList()).toList() }
                    .forEach { issue->
                        jiraGroups[issue.key]?.forEach { it.title = issue.fields.summary }
                    }
        }
    }
}