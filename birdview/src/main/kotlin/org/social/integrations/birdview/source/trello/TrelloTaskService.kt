package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.DocumentGroupId
import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.config.BVTrelloConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider

import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskListsDefaults
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.trello.model.TrelloCard
import org.social.integrations.birdview.source.trello.model.TrelloCardsSearchResponse
import org.social.integrations.birdview.utils.BVFilters
import org.social.integrations.tools.WebTargetFactory
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Named

@Named
class TrelloTaskService(
        val sourcesConfigProvider: BVSourcesConfigProvider,
        val tokenizer: TextTokenizer,
        val sourceConfig: BVTaskListsDefaults,
        val userConfigProvider: BVUsersConfigProvider
) : BVTaskSource {
    companion object {
        val TRELLO_BOARD_TYPE = "trelloBoardId"
        val TRELLO_LABEL_TYPE = "trelloLabel"
    }
    //private val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME//java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")2020-04-29T04:12:34.125Z
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
            sourcesConfigProvider.getConfigsOfType(BVTrelloConfig::class.java)
                    .flatMap { config-> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, trelloConfig: BVTrelloConfig): List<BVDocument> {
        val status = request.status
        val listName = getList(status)

        val trelloRestTarget = WebTargetFactory(trelloConfig.baseUrl)
                .getTarget("/1")
                .queryParam("key", trelloConfig.key)
                .queryParam("token", trelloConfig.token)
        val trelloIssuesResponse = trelloRestTarget.path("search")
                .queryParam("query", getQuery(request, trelloConfig, listName))
                .queryParam("partial", true)
                .queryParam("cards_limit", sourceConfig.getMaxResult())
                .request()
                .get()

        if(trelloIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Trello cards: ${trelloIssuesResponse.readEntity(String::class.java)}")
        }
        val trelloCardsContainer = trelloIssuesResponse.readEntity(TrelloCardsSearchResponse::class.java)

        val tasks = trelloCardsContainer.cards.map { card ->
            val terms = tokenizer.tokenize(card.desc) + tokenizer.tokenize(card.name)
            BVDocument(
                sourceName = trelloConfig.sourceName,
                id = card.id,
                title = card.name,
                updated = parseDate(card.dateLastActivity),
                created = parseDate(card.dateLastActivity),
                httpUrl = card.url,
                body = card.desc,
                refsIds = BVFilters.filterIds(terms),
                groupIds = extractGroupIds(card))
        }
        return tasks
    }

    private fun extractGroupIds(card: TrelloCard): List<DocumentGroupId> =
            listOf(DocumentGroupId(card.idBoard, TRELLO_BOARD_TYPE)) + card.labels.map { DocumentGroupId(it.id, TRELLO_LABEL_TYPE) }

    private fun getQuery(request: TasksRequest, trelloConfig: BVTrelloConfig, listName: String?): String =
        "@${getUser(request.user, trelloConfig.sourceName)}" +
                (listName?.let { " list:\"${listName}\"" } ?: "") +
                " edited:${getDaysBackFromNow(request.since)}" +
                " sort:edited"

    private fun getUser(userAlias: String?, sourceName:String): String =
            if (userAlias == null) "me"
            else userConfigProvider.getUserName(userAlias, sourceName)

    private fun getDaysBackFromNow(since: ZonedDateTime): Int =
        ChronoUnit.DAYS.between(since, ZonedDateTime.now()).toInt()

    private fun getList(status: String): String? = when (status) {
        "done" -> "Done"
        "progress" -> "Progress"
        "planned" -> "Planned"
        "backlog" -> "Backlog"
        "blocked" -> "Blocked"
        else -> null
    }

    private fun parseDate(dateString: String):Date = dateTimeFormat.parse(dateString)//Date.parse(dateString, dateTimeFormat)
}
