package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import javax.inject.Named

@Named
class BVJiraConfigProvider(val configProvider: BVSourcesConfigProvider) {
    fun getJira(): BVJiraConfig
            = configProvider.getConfig().jira
}