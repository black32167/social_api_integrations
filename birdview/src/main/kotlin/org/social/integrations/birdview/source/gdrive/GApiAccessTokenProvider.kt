package org.social.integrations.birdview.source.gdrive

import org.social.integrations.birdview.config.BVGoogleConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.tools.WebTargetFactory
import javax.inject.Named
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form

@Named
class GApiAccessTokenProvider(val bvConfigProvider: BVSourcesConfigProvider) {
    companion object {
        private const val tokenExchangeUrl = "https://oauth2.googleapis.com/token"
    }
    fun getToken(authCode:String): String? =
        bvConfigProvider.getConfigOfType(BVGoogleConfig::class.java)
        ?.let { config-> getToken(authCode, config) }

    private fun getToken(authCode: String, config: BVGoogleConfig): String =
        WebTargetFactory(tokenExchangeUrl)
                .getTarget("")
                .request()
                .post(getFormEntity(authCode, config))
                .also { response ->
                    if(response.status != 200) {
                        throw RuntimeException("Error reading Google access token: ${response.readEntity(String::class.java)}")
                    }
                }
                .readEntity(GAccessTokenResponse::class.java)
                .also {
                    println(it)
                }
                .access_token

    private fun getFormEntity(authCode:String, config: BVGoogleConfig) =
        Entity.form(Form()
                .param("client_id", config.clientId)
                .param("client_secret", config.clientSecret)
                .param("code", authCode)
                .param("grant_type", "authorization_code")
                .param("redirect_uri", config.redirectUri))
}