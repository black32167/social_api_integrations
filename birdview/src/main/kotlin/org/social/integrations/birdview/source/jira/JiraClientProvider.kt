package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider
import org.social.integrations.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class JiraClientProvider(
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getJiraClient(jiraConfig:BVJiraConfig) =
            JiraClient(jiraConfig, taskListDefaults, usersConfigProvider)
}