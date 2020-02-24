package org.social.integration.mattermost

import org.junit.Assert.assertNotNull
import org.junit.Test
import social.api.auth.client.AuthApi
import social.api.auth.model.UserAuthenticationRequest
import social.api.message.ApiClient
import social.api.message.client.MessageApi
import social.api.message.model.Message
import social.api.user.client.UserApi

class MattermostIT {
    val userApi = UserApi(social.api.user.ApiClient().setBasePath(DEFAULT_BASE_URL))
    val authApi = AuthApi(social.api.auth.ApiClient().setBasePath(DEFAULT_BASE_URL))

    @Test
    fun testAuth() {
        var auth = authApi.authenticateUser(UserAuthenticationRequest()
                .userName("test")
                .password("123456"))
        assertNotNull(auth)

        val messageClient = ApiClient().setBasePath(DEFAULT_BASE_URL)
        val messageApi = MessageApi(messageClient)
        messageClient.setBearerToken(auth.token)
        val message = messageApi.createMessage(Message()
                .sender("test")
                .recipient("test1")
                .messageBody("Hello"))
        assertNotNull(message.id)
    }

    @Test
    fun testOutgoing() {
        var auth = authApi.authenticateUser(UserAuthenticationRequest()
                .userName("test")
                .password("123456"))
        assertNotNull(auth)

        val messageClient = ApiClient().setBasePath(DEFAULT_BASE_URL)
        val messageApi = MessageApi(messageClient)
        messageClient.setBearerToken(auth.token)

        val messages = messageApi.getOutgoingMessages()

        assertNotNull(messages)
    }
}