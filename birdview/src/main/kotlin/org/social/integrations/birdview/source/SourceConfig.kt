package org.social.integrations.birdview.source

import javax.inject.Named

@Named
class SourceConfig {
    fun getMaxResult() = 100
}