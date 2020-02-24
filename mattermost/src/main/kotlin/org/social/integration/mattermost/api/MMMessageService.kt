package org.social.integration.mattermost.api

import org.social.integration.mattermost.ApiHttpService
import org.social.integration.mattermost.dto.MMChannelResponse
import org.social.integration.mattermost.dto.MMPostRequest
import org.social.integration.mattermost.dto.MMPostResponse
import org.social.integration.mattermost.dto.MMPostsResponse
import org.social.integration.mattermost.dto.MMTeamResponse
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

        // Establish direct channel between sender and recipient
        val channel = httpClient.postArray(
                "channels/direct",
                arrayOf(sender.id, recipient.id),
                MMChannelResponse::class.java)

        // Send message
        val mmMessage = httpClient.post(
                "posts",
                MMPostRequest(channel.id, message.messageBody!!),
                MMPostResponse::class.java)
        return message.id(mmMessage.id)
    }

    override fun getOutgoingMessages(): Messages {
        // Get channels for user
        val teams = httpClient.get("users/me/teams", Array<MMTeamResponse>::class.java);
        var posts : Collection<MMPostResponse> = teams.flatMap { team ->
            val channels = httpClient.get("users/me/teams/${team.id}/channels", Array<MMChannelResponse>::class.java)
            val directChannelIds = channels.filter { it.type == "D" }.map { it.id }
            val postsR:List<MMPostResponse> = directChannelIds.flatMap { channelId->
                val postsContainer = httpClient.get("/channels/${channelId}/posts", MMPostsResponse::class.java)
                postsContainer.posts.values.toList()
            }
            postsR
        }
        return Messages().messages(posts.map { toMessage(it) })
    }

    //TODO: retrive users in bulk
    fun toMessage(post:MMPostResponse):Message =
        Message()
                .id(post.id)
                .messageBody(post.message)
                .sender(mmUserAPI.getUserById(post.user_id).username)

    override fun getIncomingMessages(): Messages {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}