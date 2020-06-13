package org.social.integrations.birdview.source.gdrive

import org.social.integrations.birdview.config.BVGoogleConfig
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.gdrive.model.GDriveActivityRequest
import org.social.integrations.birdview.source.gdrive.model.GDriveActivityResponse
import org.social.integrations.birdview.source.gdrive.model.GDriveFileListResponse
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BearerAuth
import javax.ws.rs.client.Entity

class GDriveClient(
        val accessTokenProvider: GApiAccessTokenProvider,
        val config: BVGoogleConfig
) {
    companion object {
        private val SCOPE_DRIVE = "https://www.googleapis.com/auth/drive"
    }
    private val targetFactoryV2 = WebTargetFactory("https://driveactivity.googleapis.com/v2") { authCodeProvider(SCOPE_DRIVE) }
    private val targetFactoryV3 = WebTargetFactory("https://www.googleapis.com/drive/v3") { authCodeProvider(SCOPE_DRIVE) }

    fun getActivity(taskRequest:TasksRequest): GDriveActivityResponse =
        targetFactoryV2
                .getTarget("/activity:query")
                .request()
                .post(Entity.json(GDriveActivityRequest.from(taskRequest)))
                .also { response ->
                    if(response.status != 200) {
                        throw RuntimeException("Error reading Github issues: ${response.readEntity(String::class.java)}")
                    }
                }
                .readEntity(GDriveActivityResponse::class.java)

    fun getFiles(taskRequest:TasksRequest): GDriveFileListResponse =
            targetFactoryV3
                    .getTarget("/files")
                    .queryParam("includeItemsFromAllDrives", true)
                    .queryParam("supportsAllDrives", true)
                    .queryParam("orderBy", "modifiedTime")
                    .queryParam("fields", "files(id,name,modifiedTime)")
                    .queryParam("q", getFileListQuery(taskRequest))
                    .request()
                    .get()
                    .also { response ->
       //                 println(response.readEntity(String::class.java))
                        if(response.status != 200) {
                            throw RuntimeException("Error reading Github issues: ${response.readEntity(String::class.java)}")
                        }
                    }
                    .readEntity(GDriveFileListResponse::class.java)

    private fun authCodeProvider(scope:String) =
            accessTokenProvider.getToken(config, scope)
            ?.let(::BearerAuth)
            ?: throw RuntimeException("Failed retrieving Google API access token")

    private fun getFileListQuery(taskRequest: TasksRequest): String =
            "'me' in owners AND mimeType='application/vnd.google-apps.document'" //TODO: use taskRequest

}