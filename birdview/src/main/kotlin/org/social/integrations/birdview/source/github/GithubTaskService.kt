package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVGithubConfig
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTerm
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.SourceConfig
import org.social.integrations.birdview.source.github.model.GithubIssue
import org.social.integrations.birdview.source.github.model.GithubPRResponse
import org.social.integrations.birdview.utils.BVConcurrentUtils
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BasicAuth
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named

@Named
class GithubTaskService(
        githubConfigProvider: BVGithubConfigProvider,
        val tokenizer: TextTokenizer,
        val sourceConfig: SourceConfig
): BVTaskSource {
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())
    private val githibConfig:BVGithubConfig? = githubConfigProvider.getGithub()

    override fun getTasks(request: TasksRequest): List<BVTask> {
        val status = request.status
        val issueState = getIssueState(status)
        if(issueState == null || githibConfig == null) {
            return listOf()
        }

        val githubRestTarget = getTarget(githibConfig.baseUrl)
        val githubIssuesResponse = githubRestTarget.path("issues")
                .queryParam("filter", "created")
                .queryParam("state", issueState)
                .queryParam("per_page", sourceConfig.getMaxResult())
                .queryParam("since", request.since.format(DateTimeFormatter.ISO_DATE_TIME))
                .request()
                .get()

        if(githubIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Github issues: ${githubIssuesResponse.readEntity(String::class.java)}")
        }

        val githubIssuesContainer = githubIssuesResponse.readEntity(Array<GithubIssue>::class.java)

        // TODO: zip with issue's body?
        val prsFutures = githubIssuesContainer
                .map { it.pull_request?.url }
                .filterNotNull()
                .map {pr_url -> executor.submit (Callable<GithubPRResponse> { getPullRequest( pr_url ) }) }

        val tasks = prsFutures
                .map { future -> future.get() }
                .map { pr: GithubPRResponse -> BVTask(
                    id = pr.id,
                    title = pr.title,
                    description = pr.body,
                    updated = dateTimeFormat.parse(pr.updated_at),
                    created = dateTimeFormat.parse(pr.created_at),
                    httpUrl = pr.html_url,
                    priority = 2
                ).also { it.addTerms(extractTerms(pr)) } }
        return tasks
    }

    private fun extractTerms(pr: GithubPRResponse): List<BVTerm> {
        return tokenizer.tokenize(pr.title) + tokenizer.tokenize(pr.body?:"")
    }

    private fun getIssueState(status: String) =
        when(status) {
            "done" ->  "closed"
            "progress" ->  "open"
            else -> null
        }

    private fun getPullRequest(prUrl: String): GithubPRResponse {
        val prResponse = getTarget(prUrl).request().get();
        //println(prResponse.readEntity(String::class.java))
        return prResponse.readEntity(GithubPRResponse::class.java)
    }

    private fun getTarget(url: String) =
        WebTargetFactory(url) {
            BasicAuth(githibConfig!!.user, githibConfig.token)
        }.getTarget("")
}