package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTerm
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.SourceConfig
import org.social.integrations.birdview.source.github.model.GithubIssue
import javax.inject.Named

@Named
class GithubTaskService(
        val githubClient: GithubClient,
        val tokenizer: TextTokenizer,
        val sourceConfig: SourceConfig
): BVTaskSource {
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")


    override fun getTasks(request: TasksRequest): List<BVTask> =
         getIssueState(request.status)
                ?.let { issueState ->
                    if (request.user == null) {
                        githubClient.getCurrentUserIssues(issueState, request.since)
                    } else {
                        githubClient.getRepositoriesPullRequests(issueState, request.since, request.user)
                    }
                }
                ?.map { pr: GithubIssue -> BVTask(
                    id = pr.id,
                    title = pr.title,
                    description = pr.body,
                    updated = dateTimeFormat.parse(pr.updated_at),
                    created = dateTimeFormat.parse(pr.created_at),
                    httpUrl = pr.pull_request?.html_url ?: "---",
                    priority = 2
                ).also { it.addTerms(extractTerms(pr)) } }
                 ?: listOf()



    private fun extractTerms(pr: GithubIssue): List<BVTerm> {
        return tokenizer.tokenize(pr.title) + tokenizer.tokenize(pr.body?:"")
    }

    private fun getIssueState(status: String) =
        when(status) {
            "done" ->  "closed"
            "progress" ->  "open"
            else -> null
        }



}