package org.social.integration.mattermost.filter

import social.api.server.auth.ApiAuthContext
import social.api.server.auth.BearerAuth
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter

class ClientAuthFilter: ClientRequestFilter {
    override fun filter(requestContext: ClientRequestContext) {
        val auth = ApiAuthContext.getAuth()
        val bearerAuth = auth?.takeIf { it is BearerAuth } as BearerAuth?
        bearerAuth?.bearerToken?.also { bearerToken->
            requestContext.headers.add("Authorization", "Bearer ${bearerToken}")
        }
    }
}