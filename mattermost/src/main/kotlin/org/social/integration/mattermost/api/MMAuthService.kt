package org.social.integration.mattermost.api

import org.social.integration.mattermost.ApiHttpService
import social.api.auth.model.UserAuthenticationRequest
import social.api.auth.model.UserAuthenticationResponse
import social.api.auth.server.AuthApiService
import javax.ws.rs.client.Entity

class MMAuthService(private val httpClient: ApiHttpService) : AuthApiService {
    override fun authenticateUser(authRequest: UserAuthenticationRequest): UserAuthenticationResponse {
        val authMsg ="{\"login_id\":\"${authRequest.userName}\",\"password\":\"${authRequest.password}\"}"
        val resp = httpClient.postEntity("users/login", Entity.text(authMsg))
        val token = resp.headers.getFirst("Token") as String?
        return UserAuthenticationResponse().token(token)
    }
}