package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTerm
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.jira.model.JiraIssue
import javax.inject.Named

@Named
class JiraTaskService(
        private val jiraClientProvider: JiraClientProvider,
        private val tokenizer: TextTokenizer,
        sourcesConfigProvider: BVSourcesConfigProvider
): BVTaskSource {
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private val jiraConfigs: List<BVJiraConfig> = sourcesConfigProvider.getConfigsOfType(BVJiraConfig::class.java)

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVTask> =
        jiraConfigs.flatMap { config -> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, config: BVJiraConfig): List<BVTask> {
        val jiraIssues = jiraClientProvider.getJiraClient(config).findIssues(JiraIssuesFilter(
                request.user,
                getIssueStatus(request.status),
                request.since))

        val tasks = jiraIssues.map { issue ->
            BVTask(
                    sourceName = config.sourceName,
                    id = issue.key,
                    title = issue.fields.summary,
                    updated = dateTimeFormat.parse(issue.fields.updated),
                    created = dateTimeFormat.parse(issue.fields.created),
                    httpUrl = "${config.baseUrl}/browse/${issue.key}",
                    priority = 1
            ).also { it.addTerms(extractTerms(issue)) }
        }
        return tasks
    }


    private fun getIssueStatus(status: String): String? = when (status) {
        "done" -> "Done"
        "progress" -> "In Progress"
        "planned" -> "To Do"
        "backlog" -> "Backlog"
        "blocked" -> "Blocked"
        else -> null
    }

    private fun extractTerms(issue: JiraIssue): List<BVTerm> {
        val terms = mutableListOf<BVTerm>()
        if (issue.fields.parent != null || issue.fields.customfield_10007 != null) {
            issue.fields.parent?.also { terms.add(BVTerm(it.key, 1000.0)) }
            issue.fields.customfield_10007?.also { terms.add(BVTerm(it, 1000.0)) }
        } else {
            terms.add(BVTerm(issue.key, 100.0))
            terms.addAll(tokenizer.tokenize(issue.fields.summary))
        }
        return terms
    }
}