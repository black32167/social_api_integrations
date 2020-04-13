package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.config.BVSourcesConfigProvider
import javax.inject.Named

@Named
class BVGithubConfigProvider(val configProvider: BVSourcesConfigProvider) {
    fun getGithub() = configProvider.getConfig().github
}