package org.social.integration.mattermost.api

import org.social.integration.mattermost.ApiHttpService
import org.social.integration.mattermost.dto.MMChannelCreatedResponse
import org.social.integration.mattermost.dto.MMPostRequest
import org.social.integration.mattermost.dto.MMPostResponse
import org.social.integration.mattermost.service.user.MMUserApi
import social.api.message.model.Message
import social.api.message.model.Messages
import social.api.message.server.MessageApiService

class MMMessageService(private val httpClient: ApiHttpService) : MessageApiService {
    private val mmUserAPI = MMUserApi(httpClient)

    override fun getMessage(messageId: String?): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createMessage(message: Message): Message {
        val sender = mmUserAPI.getUserByName(message.sender!!)
        val recipient = mmUserAPI.getUserByName(message.recipient!!)
        val channel = httpClient.postArray(
                "channels/direct",
                arrayOf(sender.id, recipient.id),
                MMChannelCreatedResponse::class.java)
        val mmMessage = httpClient.post(
                "posts",
                MMPostRequest(channel.id, message.messageBody!!),
                MMPostResponse::class.java)
        return message.id(mmMessage.id)
    }

    override fun getOutgoingMessages(): Messages {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIncomingMessages(): Messages {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}