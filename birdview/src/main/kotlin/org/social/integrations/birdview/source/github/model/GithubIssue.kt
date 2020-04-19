package org.social.integrations.birdview.source.github.model

class GithubIssue (
        val pull_request : GithubPullRequestRef?
)

class GithubPullRequestRef (
        val url: String
)