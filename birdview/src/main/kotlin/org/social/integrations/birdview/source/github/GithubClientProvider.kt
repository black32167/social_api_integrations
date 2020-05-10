package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.config.BVGithubConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider
import org.social.integrations.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class GithubClientProvider(
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getGithubClient(githubConfig: BVGithubConfig) =
            GithubClient(githubConfig, taskListDefaults, usersConfigProvider)
}