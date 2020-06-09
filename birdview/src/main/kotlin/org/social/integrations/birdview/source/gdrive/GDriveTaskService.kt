package org.social.integrations.birdview.source.gdrive

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import javax.inject.Named

@Named
class GDriveTaskService(
        val authorizationCodeProvider: GApiAuthorizationCodeProvider,
        val accessTokenProvider: GApiAccessTokenProvider
) : BVTaskSource {
    private val authorizationCodeListingPort = 8082

    override fun getTasks(request: TasksRequest): List<BVDocument> {
        //TODO("Not yet implemented")
        val accessToken = authorizationCodeProvider.getAuthCode()
                ?.let(accessTokenProvider::getToken)
        println(accessToken)

        //TODO: read drive activity
        return listOf()
    }
}