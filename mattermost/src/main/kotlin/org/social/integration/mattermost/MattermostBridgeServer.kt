package org.social.integration.mattermost

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.social.integration.mattermost.api.MMAuthService
import org.social.integration.mattermost.api.MMMessageService
import org.social.integration.mattermost.api.MMUserService
import org.social.integrations.tools.WebTargetFactory
import social.api.auth.server.AuthApiResource
import social.api.message.server.MessageApiResource
import social.api.server.JaxRsServer
import social.api.server.auth.ApiAuthContext
import social.api.user.server.UserApiResource
import java.io.IOException

class MattermostBridgeServer(
        val baseBridgeServerUri: String,
        val mmUrl:String = "http://localhost:8065/api/v4") {
    private var jaxRsServer: JaxRsServer? = null

    @Throws(IOException::class)
    fun start() {
        check(jaxRsServer == null) { "Server is already run" }

        val jp = JacksonJsonProvider(ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(KotlinModule()))

        jaxRsServer = JaxRsServer(baseBridgeServerUri)
                .instances(resources())
                .instances(arrayOf(jp))
                .property("jersey.config.jsonFeature", "disabled")
                .start()
    }

    val targetFactory = WebTargetFactory(mmUrl) {ApiAuthContext.getAuth()}
    val httpService = ApiHttpService(targetFactory)
    fun resources() = arrayOf<Any>(
            MessageApiResource(MMMessageService(httpService)),
            UserApiResource(MMUserService(httpService)),
            AuthApiResource(MMAuthService(httpService)),
            WebApplicationExceptionMapper()
    )

    fun shutdown() {
        jaxRsServer!!.shutdown()
        jaxRsServer = null
    }
}