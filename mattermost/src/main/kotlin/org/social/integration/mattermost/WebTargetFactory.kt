package org.social.integration.mattermost

import org.glassfish.jersey.client.ClientConfig
import org.social.integration.mattermost.filter.ClientAuthFilter
import java.net.URI
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget

class WebTargetFactory(mmURL:String) {
    private val client = buildClient(mmURL)

    private fun buildClient(mmURL: String): Client =
        ClientBuilder.newClient(ClientConfig(
                ClientAuthFilter()))

    private val baseTarget = client.target(URI.create(mmURL))

    fun getTarget(subPath: String): WebTarget {
        return baseTarget.path(subPath)
    }
}