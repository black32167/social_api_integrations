package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.config.BVSourcesConfigProvider
import javax.inject.Named

@Named
class BVTrelloConfigProvider(val configProvider: BVSourcesConfigProvider) {
    fun getTrello() = configProvider.getConfig().trello
}