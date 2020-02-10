package org.social.integration.mattermost

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class WebApplicationExceptionMapper : ExceptionMapper<WebApplicationException> {
    class ErrorResponse (val status:Int, val message:String)

    override fun toResponse(exception: WebApplicationException): Response {
        val originalResponse = exception.response
        val status = originalResponse.status
        val message = exception.message ?: "Error status:${status}"
        val entity = ErrorResponse(status, message)

        return Response.fromResponse(originalResponse).status(status).entity(entity).build()
    }
}