package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class JiraClientProvider(
        private val taskListDefaults: BVTaskListsDefaults
) {
    fun getJiraClient(jiraConfig:BVJiraConfig) =
            JiraClient(jiraConfig, taskListDefaults)
}