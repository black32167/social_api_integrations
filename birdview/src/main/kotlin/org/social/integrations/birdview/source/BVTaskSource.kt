package org.social.integrations.birdview.source

import org.social.integrations.birdview.model.BVTask

interface BVTaskSource {
    fun getTasks(status:String):List<BVTask>
}