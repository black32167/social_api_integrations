package org.social.integrations.birdview.source.github

import java.time.ZonedDateTime

class GithubIssuesFilter (
        val repository:String?,
        val prState: String?,
        val since: ZonedDateTime?,
        val user:String
)
