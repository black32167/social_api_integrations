package org.social.integration.mattermost

import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ApiHttpService(val targetFactory:WebTargetFactory) {
    fun <T> post(path:String, payload:Any, responsePayloadClass: Class<T>): T
            = postEntity(path, Entity.json(payload)).readEntity(responsePayloadClass)

    fun <T> postArray(path: String, array: Array<*>, responsePayloadClass: Class<T>)
            = postEntity(path, Entity.json(array)).readEntity(responsePayloadClass)

    fun postString(path:String, payload:String): String
            = postEntity(path, Entity.text(payload)).readEntity(String::class.java)

    fun postEntity(path:String, entity: Entity<*>): Response {
        val resp = request(path).post(entity)
        if(resp.status >= 400) {
            throw WebApplicationException(resp)
        }
        return resp
    }

    fun request(path:String)
            = targetFactory.getTarget(path).request(MediaType.APPLICATION_JSON)

    fun getString(path: String): String {
        return getResponse(path).readEntity(String::class.java)
    }

    fun <T> get(path: String, responsePayloadClass: Class<T>): T {
        return getResponse(path).readEntity(responsePayloadClass)
    }

    fun getResponse(path:String): Response {
        val resp = request(path).get()
        if(resp.status >= 400) {
            throw WebApplicationException(resp)
        }
        return resp
    }
}