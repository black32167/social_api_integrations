package org.social.integration.mattermost.service.user

import org.social.integration.mattermost.ApiHttpService
import javax.ws.rs.NotFoundException

class MMUserApi(private val httpClient: ApiHttpService) {
    fun getUserByName(userName: String): UserResponseItem =
        httpClient.postArray("/users/usernames", arrayOf(userName), Array<UserResponseItem>::class.java)
                .takeIf { it.isNotEmpty() }
                ?.let { it[0] }
                ?: throw NotFoundException("User not found: ${userName}")
}