package org.social.integrations.birdview.source.github

import org.social.integrations.birdview.config.BVGithubConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider
import org.social.integrations.birdview.source.BVTaskListsDefaults
import org.social.integrations.birdview.source.github.model.GithubIssue
import org.social.integrations.birdview.source.github.model.GithubPRResponse
import org.social.integrations.birdview.source.github.model.GithubSearchIssuesResponse
import org.social.integrations.birdview.utils.BVConcurrentUtils
import org.social.integrations.tools.WebTargetFactory
import social.api.server.auth.BasicAuth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class GithubClient(
        val githubConfig: BVGithubConfig,
        val sourceConfig: BVTaskListsDefaults,
        val usersConfigProvider: BVUsersConfigProvider
) {
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    fun getCurrentUserIssues(issueState: String, since:ZonedDateTime):List<GithubIssue> =
        getTarget()
            ?.let { githubRestTarget->
                val githubIssuesResponse = githubRestTarget.path("issues")
                        .queryParam("filter", "created")
                        .queryParam("state", issueState)
                        .queryParam("per_page", sourceConfig.getMaxResult())
                        .queryParam("since", since.format(DateTimeFormatter.ISO_DATE_TIME))
                        .request()
                        .get()

                if(githubIssuesResponse.status != 200) {
                    throw RuntimeException("Error reading Github issues: ${githubIssuesResponse.readEntity(String::class.java)}")
                }

                return githubIssuesResponse.readEntity(Array<GithubIssue>::class.java).toList()
            }
            ?: listOf<GithubIssue>()

    fun getRepositoriesPullRequests(issueState: String, since: ZonedDateTime, user:String):List<GithubIssue> =
        getConfig()
                ?.repositories
                ?.map { repository ->
                    executor.submit(Callable {
                        findIssues(GithubIssuesFilter(
                                prState = issueState,
                                since = since,
                                userAlias = user))
                    })
                }
                ?.flatMap { it.get() }
        ?: listOf()

    private fun findIssues(filter:GithubIssuesFilter):List<GithubIssue> =
        getTarget()
                ?.path("search")
                ?.path("issues")
                ?.queryParam("q", getFilterQuery(filter))
                ?.also {
                    println("Url:${it.uri}")
                }
                ?.request()
                ?.get()
                ?.also {
                    if(it.status != 200) {
                        throw java.lang.RuntimeException("Status:${it.status}, message=${it.readEntity(String::class.java)}")
                    }
//                    println(it.readEntity(String::class.java))
//                    println()
                }
                ?.readEntity(GithubSearchIssuesResponse::class.java)?.items
                ?.asList()
                ?:listOf()

    private fun getFilterQuery(filter: GithubIssuesFilter): String =
            "type:pr" +
            (filter.prState?.let {" state:${it}"} ?: "") +
            (filter.repository?.let { " repo:${it}" } ?: "") +
            filter.userAlias.let { " author:${getGithubUser(it)}" } +
            (filter.since?.let { " updated:>=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "")

    private fun getGithubUser(userAlias: String): String? =
            usersConfigProvider.getUserName(userAlias, githubConfig.sourceName)

    private fun getUserIdByEMail(email: String): String =
            getTarget()
                    ?.path("search")
                    ?.path("users")
                    ?.queryParam("q", "${email} in:email")
                    ?.request()
                    ?.get()
                    ?.also {
                        println("Status:${it.status}")
                        println(it.readEntity(String::class.java))
                        println("")
                    }
                    ?.readEntity(String::class.java)
                    ?:""

    fun getCurrentUserPullRequests(issueState: String, since: ZonedDateTime):List<GithubPRResponse> =
            getCurrentUserIssues(issueState, since)
                    .mapNotNull { issue -> // Get Pull Requests
                        issue.pull_request?.url
                                ?.let { pr_url -> executor.submit(Callable<GithubPRResponse> { getPullRequest(pr_url) }) }
                    }
                    .map { it.get() }

    private fun getConfig(): BVGithubConfig?  = githubConfig

    fun getPullRequest(prUrl: String): GithubPRResponse? =
            getTarget(prUrl)
                    ?.request()
                    ?.get()
                    ?.readEntity(GithubPRResponse::class.java)

    private fun getTarget() = getConfig()
        ?.let { config-> getTarget(config.baseUrl) }

    private fun getTarget(url:String) = getConfig()
        ?.let { config-> WebTargetFactory(url) {
            BasicAuth(config.user, config.token)
        }.getTarget("") }

}