package org.social.integration.mattermost

/**
 * Mattermost API proxy executor.
 */
fun main() {
    val serverUrl = System.getProperty("mattermost.api.proxy.url","http://localhost:8083")
    MattermostBridgeServer(serverUrl).start()
}