package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.source.jira.model.JiraIssuesFilterRequest
import org.social.integrations.birdview.source.jira.model.JiraIssuesFilterResponse
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BasicAuth
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import javax.inject.Named
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget

@Named
class JiraTaskService(
        jiraConfigProvider: BVJiraConfigProvider
): TaskApiService {
    private val jiraConfig: BVJiraConfig = jiraConfigProvider.getJira()
    private val jiraRestTarget: WebTarget = WebTargetFactory(jiraConfig.baseUrl) {
        BasicAuth(jiraConfig.user, jiraConfig.token)
    }.getTarget("/rest/api/2")

    override fun getTask(p0: String?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTask(p0: Task?): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTasks(status: String): Tasks {
        val jiraIssuesResponse = jiraRestTarget.path("search").request().post(Entity.json(JiraIssuesFilterRequest(
                maxResults = 10,
                jql = "(assignee = currentUser() or watcher = currentUser()) and status in (\"${status}\") order by lastViewed DESC"
        )))

        if(jiraIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Jira tasks: ${jiraIssuesResponse.readEntity(String::class.java)}")
        }

        val jiraIssuesContainer = jiraIssuesResponse.readEntity(JiraIssuesFilterResponse::class.java)

        val tasks = jiraIssuesContainer.issues.map { issue -> Task().apply {
            id = issue.key
            updated = issue.fields.updated
            httpUrl = "${jiraConfig.baseUrl}/browse/${issue.key}"
        } }
        return Tasks().apply { setTasks(tasks) }
    }
}