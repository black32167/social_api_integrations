package org.social.integrations.birdview.source.github.model

class GithubIssue (
        val id: String,
        val pull_request : GithubPullRequestRef?,
        var body: String?,
        var title: String,
        val updated_at : String,
        val created_at : String
)

class GithubPullRequestRef (
        val url: String,
        var html_url: String
)