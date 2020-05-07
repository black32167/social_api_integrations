package org.social.integrations.birdview.source.github.model

class GithubIssue (
        val pull_request : GithubPullRequestRef?,
        var body: String?
)

class GithubPullRequestRef (
        val url: String
)