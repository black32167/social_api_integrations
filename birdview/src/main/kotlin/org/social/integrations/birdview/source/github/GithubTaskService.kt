package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.source.github.model.GithubIssue
import org.social.integrations.birdview.source.github.model.GithubPRResponse
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BasicAuth
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named

@Named
class GithubTaskService(
        githubConfigProvider: BVGithubConfigProvider
): TaskApiService {
    private val executor = Executors.newCachedThreadPool()
    private val maxResults = 10;
    private val githibConfig = githubConfigProvider.getGithub()
    private val githubRestTarget = getTarget(githibConfig.baseUrl)

    override fun getTask(p0: String?): Task {
        TODO("Not yet implemented")
    }

    override fun createTask(p0: Task?): Task {
        TODO("Not yet implemented")
    }

    override fun getTasks(status: String): Tasks {
        val issueState = getIssueState(status)
        val githubIssuesResponse = githubRestTarget.path("issues")
                .queryParam("filter", "created")
                .queryParam("state", issueState)
                .request()
                .get()

        if(githubIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Github issues: ${githubIssuesResponse.readEntity(String::class.java)}")
        }

        val githubIssuesContainer = githubIssuesResponse.readEntity(Array<GithubIssue>::class.java)

        val prsFutures = githubIssuesContainer.map {
            executor.submit (object: Callable<GithubPRResponse> {
                override fun call() = getPullRequest(it.pull_request.url)
            })
        }

        val tasks = prsFutures
                .map { future -> future.get() }
                .map { pr: GithubPRResponse -> Task().apply {
                    id = pr.id
                    title = pr.title
                    updated = pr.updated_at
                    httpUrl = pr.html_url
                } }
        return Tasks().apply { setTasks(tasks) }
    }

    private fun getIssueState(status: String) =
        when(status) {
            "done" ->  "closed"
            "progress" ->  "open"
            else -> ""
        }

    private fun getPullRequest(prUrl: String): GithubPRResponse {
        val prResponse = getTarget(prUrl).request().get();

        return prResponse.readEntity(GithubPRResponse::class.java)
    }

    private fun getTarget(url: String) =
        WebTargetFactory(url) {
            BasicAuth(githibConfig.user, githibConfig.token)
        }.getTarget("")
}