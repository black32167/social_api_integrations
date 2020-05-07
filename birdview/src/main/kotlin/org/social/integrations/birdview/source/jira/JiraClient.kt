package org.social.integrations.birdview.source.jira

import org.social.integrations.birdview.source.SourceConfig
import org.social.integrations.birdview.source.jira.model.JiraIssue
import org.social.integrations.birdview.source.jira.model.JiraIssuesFilterRequest
import org.social.integrations.birdview.source.jira.model.JiraIssuesFilterResponse
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BasicAuth
import javax.inject.Named
import javax.ws.rs.client.Entity

@Named
class JiraClient(
        jiraConfigProvider: BVJiraConfigProvider,
        val sourceConfig: SourceConfig) {
    private val jiraConfig = jiraConfigProvider.getJira()

    fun findIssues(jql: String): Array<JiraIssue> {
        if(jiraConfig == null) {
            return arrayOf()
        }

        val jiraRestTarget = WebTargetFactory(jiraConfig.baseUrl) {
            BasicAuth(jiraConfig.user, jiraConfig.token)
        }.getTarget("/rest/api/2")

        val jiraIssuesResponse = jiraRestTarget.path("search").request().post(Entity.json(JiraIssuesFilterRequest(
                maxResults = sourceConfig.getMaxResult(),
                fields = arrayOf("*all"),
                jql = jql
        )))

        if(jiraIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Jira tasks: ${jiraIssuesResponse.readEntity(String::class.java)}")
        }

        // println(jiraIssuesResponse.readEntity(String::class.java))

        return jiraIssuesResponse.readEntity(JiraIssuesFilterResponse::class.java).issues
    }

    fun loadIssues(keys:List<String>): Array<JiraIssue> {
        val issues = findIssues("issueKey IN (${keys.joinToString(",")})");
        return issues;
    }
}