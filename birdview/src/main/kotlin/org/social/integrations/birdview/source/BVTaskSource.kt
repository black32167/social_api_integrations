package org.social.integrations.birdview.source

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.request.TasksRequest

interface BVTaskSource {
    fun getTasks(request: TasksRequest):List<BVDocument>
}