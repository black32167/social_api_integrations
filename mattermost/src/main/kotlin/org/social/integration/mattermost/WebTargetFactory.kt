package org.social.integration.mattermost

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.glassfish.jersey.client.ClientConfig
import org.social.integration.mattermost.filter.ClientAuthFilter
import java.net.URI
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget

class WebTargetFactory(mmURL:String) {
    private val client = buildClient(mmURL)

    private fun buildClient(mmURL: String): Client =
        ClientBuilder.newClient(
                ClientConfig(ClientAuthFilter())
                        .property("jersey.config.jsonFeature", "disabled")
                        .register(JacksonJsonProvider(ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                .registerModule(KotlinModule())))
        )

    private val baseTarget = client.target(URI.create(mmURL))

    fun getTarget(subPath: String): WebTarget {
        return baseTarget.path(subPath)
    }
}