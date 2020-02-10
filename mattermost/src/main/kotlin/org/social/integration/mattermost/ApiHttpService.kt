package org.social.integration.mattermost

import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ApiHttpService(val targetFactory:WebTargetFactory) {
    fun post(path:String, payload:Entity<*>): Response {
        val resp = request(path).post(payload)
        if(resp.status >= 400) {
            throw WebApplicationException(resp)
        }
        return resp
    }

    fun request(path:String) = targetFactory.getTarget(path).request(MediaType.APPLICATION_JSON)
}