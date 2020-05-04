package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTerm
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.SourceConfig
import org.social.integrations.birdview.source.jira.model.JiraIssue
import org.social.integrations.birdview.source.jira.model.JiraIssuesFilterRequest
import org.social.integrations.birdview.source.jira.model.JiraIssuesFilterResponse
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BasicAuth
import javax.inject.Named
import javax.ws.rs.client.Entity

@Named
class JiraTaskService(
        jiraConfigProvider: BVJiraConfigProvider,
        val tokenizer: TextTokenizer,
        val sourceConfig: SourceConfig
): BVTaskSource {
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private val jiraConfig = jiraConfigProvider.getJira()
    private val jiraRestTarget = WebTargetFactory(jiraConfig.baseUrl) {
        BasicAuth(jiraConfig.user, jiraConfig.token)
    }.getTarget("/rest/api/2")

    override fun getTasks(status: String): List<BVTask> {
        val issueStatus = getIssueStatus(status)
        if(issueStatus == null) {
            return listOf()
        }

        val jiraIssuesResponse = jiraRestTarget.path("search").request().post(Entity.json(JiraIssuesFilterRequest(
                maxResults = sourceConfig.getMaxResult(),
                fields = arrayOf("*all"),
                jql = "(assignee = currentUser() or watcher = currentUser()) and status in (\"${issueStatus}\") order by lastViewed DESC"
        )))

        if(jiraIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Jira tasks: ${jiraIssuesResponse.readEntity(String::class.java)}")
        }

        // println(jiraIssuesResponse.readEntity(String::class.java))

        val jiraIssuesContainer = jiraIssuesResponse.readEntity(JiraIssuesFilterResponse::class.java)

        val tasks = jiraIssuesContainer.issues.map { issue -> BVTask(
            id = issue.key,
            title = issue.fields.summary,
            updated = dateTimeFormat.parse(issue.fields.updated),
            created = dateTimeFormat.parse(issue.fields.created),
            httpUrl = "${jiraConfig.baseUrl}/browse/${issue.key}",
            priority = 1
        ).also { it.addTerms(extractTerms(issue)) } }
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