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
    val summary: String,
    val description: String?,
    val customfield_10007: String?, //EPIC key
    val parent: JiraParentIssue?
)

class JiraParentIssue (
    val key: String,
    val fields: JiraParentIssueFields
)

class JiraParentIssueFields (
    val summary: String
)
