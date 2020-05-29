package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.DocumentGroupId
import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.jira.model.JiraIssue
import org.social.integrations.birdview.utils.BVFilters
import javax.inject.Named

@Named
class JiraTaskService(
        private val jiraClientProvider: JiraClientProvider,
        private val tokenizer: TextTokenizer,
        sourcesConfigProvider: BVSourcesConfigProvider
): BVTaskSource {
    companion object {
        val JIRA_KEY_TYPE = "jiraKey"
    }
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private val jiraConfigs: List<BVJiraConfig> = sourcesConfigProvider.getConfigsOfType(BVJiraConfig::class.java)

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
        jiraConfigs.flatMap { config -> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, config: BVJiraConfig): List<BVDocument> {
        val jiraIssues = jiraClientProvider.getJiraClient(config).findIssues(JiraIssuesFilter(
                request.user,
                getIssueStatus(request.status),
                request.since))

        val tasks = jiraIssues.map { issue ->
            val description = issue.fields.description ?: ""
            BVDocument(
                    sourceName = config.sourceName,
                    id = issue.key,
                    title = issue.fields.summary,
                    updated = dateTimeFormat.parse(issue.fields.updated),
                    created = dateTimeFormat.parse(issue.fields.created),
                    httpUrl = "${config.baseUrl}/browse/${issue.key}",
                    body = description,
                    refsIds = BVFilters.filterIds("${description} ${issue.fields.summary}"),
                    groupIds = extractGroupIds(issue, config.sourceName),
                    status = issue.fields.status.name
            )
        }
        return tasks
    }

    private fun extractGroupIds(issue: JiraIssue, sourceName: String): List<DocumentGroupId> =
            (issue.fields.customfield_10007?.let { listOf(DocumentGroupId(it, JIRA_KEY_TYPE, sourceName)) } ?: listOf<DocumentGroupId>()) +
                    (issue.fields.parent?.let{ listOf(DocumentGroupId(it.key, JIRA_KEY_TYPE, sourceName)) } ?: listOf<DocumentGroupId>())

    private fun getIssueStatus(status: String): String? = when (status) {
        "done" -> "Done"
        "progress" -> "In Progress"
        "planned" -> "To Do"
        "backlog" -> "Backlog"
        "blocked" -> "Blocked"
        else -> null
    }
}