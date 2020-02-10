package org.social.integration.mattermost.filter

import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientResponseContext
import javax.ws.rs.client.ClientResponseFilter

class StatusCodeFilter: ClientResponseFilter {
    override fun filter(requestContext: ClientRequestContext, responseContext: ClientResponseContext) {
        if(responseContext.status >= 400) {
            throw WebApplicationException(responseContext.status)
        }
    }
}