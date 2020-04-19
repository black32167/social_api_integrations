package org.social.integrations.birdview.source.jira.model

class JiraIssuesFilterResponse (
        val issues: Array<JiraIssue>
)

class JiraIssue (
    val key: String,
    val fields : JiraIssueFields
)

class JiraIssueFields (
    val updated: String,
    val created: String,
    val summary: String
)