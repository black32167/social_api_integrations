package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVTrelloConfig
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTerm
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.SourceConfig
import org.social.integrations.birdview.source.trello.model.TrelloCard
import org.social.integrations.birdview.source.trello.model.TrelloCardsSearchResponse
import org.social.integrations.tools.WebTargetFactory
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Named

@Named
class TrelloTaskService(
        trelloConfigProvider: BVTrelloConfigProvider,
        val tokenizer: TextTokenizer,
        val sourceConfig: SourceConfig
) : BVTaskSource {
    //private val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME//java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")2020-04-29T04:12:34.125Z
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private val trelloConfig: BVTrelloConfig? = trelloConfigProvider.getTrello()

    override fun getTasks(request: TasksRequest): List<BVTask> {
        val status = request.status
        val listName = getList(status)
        if(listName == null || trelloConfig == null) {
            return listOf()
        }

        val trelloRestTarget = WebTargetFactory(trelloConfig.baseUrl)
                .getTarget("/1")
                .queryParam("key", trelloConfig.key)
                .queryParam("token", trelloConfig.token)
        val trelloIssuesResponse = trelloRestTarget.path("search")
                .queryParam("query", "@me list:\"${listName}\" edited:${getDaysBackFromNow(request.since)} sort:edited")
                .queryParam("partial", true)
                .queryParam("cards_limit", sourceConfig.getMaxResult())
                .request()
                .get()

        if(trelloIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Trello cards: ${trelloIssuesResponse.readEntity(String::class.java)}")
        }
        val trelloCardsContainer = trelloIssuesResponse.readEntity(TrelloCardsSearchResponse::class.java)

        val tasks = trelloCardsContainer.cards.map { card -> BVTask(
            id = card.id,
            title = card.name,
            updated = parseDate(card.dateLastActivity),
            created = parseDate(card.dateLastActivity),
            httpUrl = card.url,
            priority = 1
        ).also { it.addTerms(extractTerms(card)) } }
        return tasks
    }

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

    private fun extractTerms(card: TrelloCard): List<BVTerm> {
        val terms = mutableListOf<BVTerm>()
        terms.add(BVTerm(card.url, 3.0))
        terms.addAll(tokenizer.tokenize(card.name))
        terms.addAll(card.labels.map { BVTerm(it.name, 2.0) })
        return terms
    }

    private fun parseDate(dateString: String):Date = dateTimeFormat.parse(dateString)//Date.parse(dateString, dateTimeFormat)
}