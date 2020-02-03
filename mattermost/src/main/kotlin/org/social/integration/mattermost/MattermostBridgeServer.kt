package org.social.integration.mattermost

import org.social.integration.mattermost.message.MattermostMessageService
import social.api.message.server.MessageApiResource
import social.api.server.JaxRsServer
import java.io.IOException

class MattermostBridgeServer(val baseUri: String) {
    private var jaxRsServer: JaxRsServer? = null
    @Throws(IOException::class)
    fun start() {
        check(jaxRsServer == null) { "Server is already run" }

        jaxRsServer = JaxRsServer(baseUri)
                .instances(resources())
           //     arrayOf(AuthFilter(UserApi(ApiClient().setBasePath(baseUri)))))
                .start()
    }

    fun resources() = arrayOf<Any>(MessageApiResource(MattermostMessageService()))

    fun shutdown() {
        jaxRsServer!!.shutdown()
        jaxRsServer = null
    }
}