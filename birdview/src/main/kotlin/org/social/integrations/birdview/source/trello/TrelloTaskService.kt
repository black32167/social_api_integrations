package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.analysis.tokenize.TextTokenizer
import org.social.integrations.birdview.model.BVTask
import org.social.integrations.birdview.model.BVTerm
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.trello.model.TrelloCard
import org.social.integrations.birdview.source.trello.model.TrelloCardsSearchResponse
import org.social.integrations.tools.WebTargetFactory
import javax.inject.Named

@Named
class TrelloTaskService(
        trelloConfigProvider: BVTrelloConfigProvider,
        val tokenizer: TextTokenizer
) : BVTaskSource {
    private val maxResults = 10;
    private val trelloConfig = trelloConfigProvider.getTrello()
    private val trelloRestTarget = WebTargetFactory(trelloConfig.baseUrl)
            .getTarget("/1")
            .queryParam("key", trelloConfig.key)
            .queryParam("token", trelloConfig.token)

    override fun getTasks(status: String): List<BVTask> {
        val listName = getList(status)
        val trelloIssuesResponse = trelloRestTarget.path("search")
                .queryParam("query", "@me list:\"${listName}\" sort:edited")
                .queryParam("partial", true)
                .queryParam("cards_limit", maxResults)
                .request()
                .get()

        if(trelloIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Trello cards: ${trelloIssuesResponse.readEntity(String::class.java)}")
        }
        val trelloCardsContainer = trelloIssuesResponse.readEntity(TrelloCardsSearchResponse::class.java)

        val tasks = trelloCardsContainer.cards.map { card -> BVTask(
            id = card.id,
            title = card.name,
            updated = card.dateLastActivity,
            created = "",
            httpUrl = card.url,
            priority = 1
        ).also { it.addTerms(extractTerms(card)) } }
        return tasks
    }

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
}