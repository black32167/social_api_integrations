package org.social.integrations.birdview.source.gdrive

import org.social.integrations.birdview.config.BVGDriveConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider
import javax.inject.Named

@Named
class GDriveClientProvider(
        private val userConfigProvider: BVUsersConfigProvider,
        private val accessTokenProvider: GApiAccessTokenProvider
) {
    fun getGoogleApiClient(config: BVGDriveConfig)
            = GDriveClient(accessTokenProvider, userConfigProvider, config)
}