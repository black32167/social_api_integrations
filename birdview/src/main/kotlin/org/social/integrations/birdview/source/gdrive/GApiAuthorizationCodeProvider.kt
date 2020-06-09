package org.social.integrations.birdview.source.gdrive

import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.Request
import org.glassfish.grizzly.http.server.Response
import org.social.integrations.birdview.config.BVGoogleConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Named
class GApiAuthorizationCodeProvider(val bvConfigProvider: BVSourcesConfigProvider) {
    companion object {
        const val PARAM_CODE = "code"
        const val PARAM_ERROR = "error"
    }

    private val timeoutMs = 99999999L//5000L

    fun getAuthCode(): String? =
                bvConfigProvider.getConfigOfType(BVGoogleConfig::class.java)
                        ?.let (this::getAuthCode)

    fun getAuthCode(config: BVGoogleConfig):String? {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return null
        }
        val httpServer = HttpServer.createSimpleServer(null, config.authorizationCodeListingPort)
        try {
            // Start server
            val completableFuture = CompletableFuture<String>()
            httpServer.serverConfiguration.addHttpHandler(object : HttpHandler() {
                override fun service(request: Request, response: Response) {
                    val maybeError:String? = request.getParameter(PARAM_ERROR)
                    if (maybeError != null) {
                        completableFuture.completeExceptionally(RuntimeException("Authentication error:${maybeError}"))
                    } else {
                        val url = request.queryString
                        println(url)
                        completableFuture.complete(request.getParameter(PARAM_CODE))
                    }
                }
            })
            httpServer.start()

            Desktop.getDesktop().browse(URI(getAuthTokenUrl(config.clientId, config.redirectUri)));

            return completableFuture.get(timeoutMs, TimeUnit.MILLISECONDS)
        } finally {
            // Shutdown server
            httpServer.shutdown()
        }
    }

    fun getAuthTokenUrl(clientId:String, redirectUri:String):String =
            "https://accounts.google.com/o/oauth2/v2/auth" +
            "?client_id=${clientId}" +
            "&response_type=code" +
            "&redirect_uri=${redirectUri}" +
            "&scope=https://www.googleapis.com/auth/drive.activity"

}
