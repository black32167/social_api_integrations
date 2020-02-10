package org.social.integration.mattermost

/**
 * Mattermost API proxy executor.
 */
const val DEFAULT_BASE_URL = "http://localhost:8083"
fun main() {
    val serverUrl = System.getProperty("mattermost.api.proxy.url", DEFAULT_BASE_URL)
    MattermostBridgeServer(serverUrl).start()
}