package org.social.integrations.birdview.source.gdrive.model

import org.social.integrations.birdview.request.TasksRequest

class GDriveActivityRequest (
    val filter: String? = null
) {
    companion object {
        fun from(taskRequest: TasksRequest) = GDriveActivityRequest(
                filter = taskRequest.user ?: "owner:me"
        )
    }
}