package org.social.integrations.birdview.source.gdrive

class GAccessTokenResponse (
        val access_token: String,
        val expires_in: Int,
        val id_token: String?,
        val scope: String,
        val token_type: String
)