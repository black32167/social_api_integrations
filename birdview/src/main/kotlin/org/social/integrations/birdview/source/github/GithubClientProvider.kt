package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.config.BVGithubConfig
import org.social.integrations.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class GithubClientProvider(
        private val taskListDefaults: BVTaskListsDefaults
) {
    fun getGithubClient(githubConfig: BVGithubConfig) =
            GithubClient(githubConfig, taskListDefaults)
}