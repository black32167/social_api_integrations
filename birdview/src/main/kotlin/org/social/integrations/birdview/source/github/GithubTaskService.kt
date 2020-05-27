package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.DocumentGroupId
import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVGithubConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.github.model.GithubIssue
import org.social.integrations.birdview.utils.BVFilters
import javax.inject.Named

@Named
class GithubTaskService(
        val sourcesConfigProvider: BVSourcesConfigProvider,
        val githubClientProvider: GithubClientProvider,
        val tokenizer: TextTokenizer
): BVTaskSource {
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
            sourcesConfigProvider.getConfigsOfType(BVGithubConfig::class.java)
                    .flatMap { config-> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, githubConfig:BVGithubConfig): List<BVDocument> =
        getIssueState(request.status)
        ?.let { status -> githubClientProvider.getGithubClient(githubConfig).getRepositoriesPullRequests(status, request.since, request.user) }
        ?.map { pr: GithubIssue ->
            val description = pr.body ?: ""
            val terms = tokenizer.tokenize(description) + tokenizer.tokenize(pr.title)
            BVDocument(
                sourceName = githubConfig.sourceName,
                id = pr.id,
                title = pr.title,
                body = description,
                updated = dateTimeFormat.parse(pr.updated_at),
                created = dateTimeFormat.parse(pr.created_at),
                httpUrl = pr.pull_request?.html_url ?: "---",
                refsIds = BVFilters.filterIds(terms),
                groupIds = extractGroupIds(pr))
        }
        ?: listOf()

    private fun extractGroupIds(issue: GithubIssue): List<DocumentGroupId> = listOf()

    private fun extractTerms(pr: GithubIssue): List<String> {
        return tokenizer.tokenize(pr.title) + tokenizer.tokenize(pr.body?:"")
    }

    private fun getIssueState(status: String):String? =
        when(status) {
            "done" ->  "closed"
            "progress" ->  "open"
            "any" -> "any"
            else -> null
        }
}