package org.social.integrations.birdview.source.gdrive

import org.social.integrations.birdview.config.BVGoogleConfig
import javax.inject.Named

@Named
class GDriveClientProvider(
        private val authorizationCodeProvider: GApiAuthorizationCodeProvider,
        private val accessTokenProvider: GApiAccessTokenProvider
) {
    fun getGoogleApiClient(config: BVGoogleConfig)
            = GDriveClient(accessTokenProvider, config)
}