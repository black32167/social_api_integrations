package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.DocumentGroupId
import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.config.BVTrelloConfig
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskListsDefaults
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.trello.model.TrelloCard
import org.social.integrations.birdview.utils.BVFilters
import java.util.*
import javax.inject.Named

@Named
class TrelloTaskService(
        val sourcesConfigProvider: BVSourcesConfigProvider,
        val tokenizer: TextTokenizer,
        val sourceConfig: BVTaskListsDefaults,
        private val trelloClientProvider: TrelloClientProvider
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

        val cards = trelloClientProvider.getTrelloClient(trelloConfig).getCards(TrelloCardsFilter(
                since = request.since,
                user = request.user,
                listName = listName
        ))

        val tasks = cards.map { card ->
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
                groupIds = extractGroupIds(card, trelloConfig.sourceName))
        }
        return tasks
    }

    private fun extractGroupIds(card: TrelloCard, sourceName: String): List<DocumentGroupId> =
            listOf(DocumentGroupId(card.idBoard, TRELLO_BOARD_TYPE, sourceName)) +
                    card.labels.map { DocumentGroupId(it.id, TRELLO_LABEL_TYPE, sourceName) }


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
