package org.social.integration.mattermost.message

import social.api.message.model.Message
import social.api.message.model.Messages
import social.api.message.server.MessageApiService

class MattermostMessageService : MessageApiService {
    override fun getMessage(messageId: String?): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createMessage(p0: Message?): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOutgoingMessages(): Messages {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIncomingMessages(): Messages {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}