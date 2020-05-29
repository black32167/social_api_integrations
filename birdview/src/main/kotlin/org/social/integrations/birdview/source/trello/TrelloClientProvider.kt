package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.config.BVTrelloConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider
import org.social.integrations.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class TrelloClientProvider(
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getTrelloClient(trelloConfig: BVTrelloConfig) =
            TrelloClient(trelloConfig, taskListDefaults, usersConfigProvider)

}