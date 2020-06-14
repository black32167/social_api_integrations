package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.BVDocumentId
import org.social.integrations.birdview.config.BVGithubConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.github.model.GithubIssue
import org.social.integrations.birdview.source.github.model.GithubPullRequest
import org.social.integrations.birdview.utils.BVConcurrentUtils
import org.social.integrations.birdview.utils.BVFilters
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class GithubTaskService(
        val sourcesConfigProvider: BVSourcesConfigProvider,
        val githubClientProvider: GithubClientProvider
): BVTaskSource {
    companion object {
        val GITHUB_ID = "githubId"
    }
    private val executor = Executors.newFixedThreadPool(10, BVConcurrentUtils.getDaemonThreadFactory())
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
            sourcesConfigProvider.getConfigsOfType(BVGithubConfig::class.java)
                    .flatMap { config-> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, githubConfig:BVGithubConfig): List<BVDocument> =
        getIssueState(request.status)
        ?.let { status -> githubClientProvider.getGithubClient(githubConfig).getPullRequestIssues(status, request.since, request.user) }
        ?.map { issue: GithubIssue -> executor.submit (Callable<GithubPullRequest> { getPr(issue, githubConfig) } ) }
        ?.map ( Future<GithubPullRequest>::get )
        ?.map { pr: GithubPullRequest ->
            val description = pr.body ?: ""
            BVDocument(
                ids = setOf(BVDocumentId( id = pr.id, type = GITHUB_ID, sourceName = githubConfig.sourceName)),
                title = pr.title,
                body = description,
                updated = dateTimeFormat.parse(pr.updated_at),
                created = dateTimeFormat.parse(pr.created_at),
                httpUrl = pr.html_url,
                refsIds = BVFilters.filterIdsFromText("${description} ${pr.title}") +
                    BVFilters.filterIdsFromText(pr.head.ref),
                groupIds = setOf(),
                status = pr.state
            )
        }
        ?: listOf()

    override fun getType() = "github"

    private fun getPr(issue: GithubIssue, githubConfig:BVGithubConfig): GithubPullRequest? =
            issue.pull_request?.url
                    ?.let { url -> githubClientProvider.getGithubClient(githubConfig).getPullRequest(url) }

    private fun getIssueState(status: String):String? =
        when(status) {
            "done" ->  "closed"
            "progress" ->  "open"
            "any" -> "any"
            else -> null
        }
}