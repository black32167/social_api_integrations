package org.social.integration.mattermost

import org.social.integration.mattermost.message.MattermostAuthService
import org.social.integration.mattermost.message.MattermostMessageService
import org.social.integration.mattermost.message.MattermostUserService
import social.api.auth.server.AuthApiResource
import social.api.message.server.MessageApiResource
import social.api.server.JaxRsServer
import social.api.user.server.UserApiResource
import java.io.IOException

class MattermostBridgeServer(val baseUri: String) {
    private var jaxRsServer: JaxRsServer? = null
    @Throws(IOException::class)
    fun start() {
        check(jaxRsServer == null) { "Server is already run" }

        jaxRsServer = JaxRsServer(baseUri)
                .instances(resources())
                .start()
    }

    // TODO: parameterize
    val mmUrl = "http://localhost:8065/api/v4"
    val targetFactory = WebTargetFactory(mmUrl)
    val httpService = ApiHttpService(targetFactory)
    fun resources() = arrayOf<Any>(
            MessageApiResource(MattermostMessageService(httpService)),
            UserApiResource(MattermostUserService(httpService)),
            AuthApiResource(MattermostAuthService(httpService)),
            WebApplicationExceptionMapper()
    )

    fun shutdown() {
        jaxRsServer!!.shutdown()
        jaxRsServer = null
    }
}