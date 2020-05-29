package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.config.BVTrelloConfig
import org.social.integrations.birdview.config.BVUsersConfigProvider
import org.social.integrations.birdview.source.BVTaskListsDefaults
import org.social.integrations.birdview.source.trello.model.TrelloBoard
import org.social.integrations.birdview.source.trello.model.TrelloCard
import org.social.integrations.birdview.source.trello.model.TrelloCardsSearchResponse
import org.social.integrations.birdview.source.trello.model.TrelloList
import org.social.integrations.tools.WebTargetFactory
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class TrelloClient(private val trelloConfig: BVTrelloConfig,
                   private val sourceConfig: BVTaskListsDefaults,
                   val usersConfigProvider: BVUsersConfigProvider) {
    fun getCards(cardsFilter: TrelloCardsFilter): List<TrelloCard> {
        val trelloCardsResponse = getTarget().path("search")
                .queryParam("query", getQuery(cardsFilter, trelloConfig))
                .queryParam("partial", true)
                .queryParam("cards_limit", sourceConfig.getMaxResult())
                .request()
                .get()

        if(trelloCardsResponse.status != 200) {
            throw RuntimeException("Error reading Trello cards: ${trelloCardsResponse.readEntity(String::class.java)}")
        }
        return trelloCardsResponse.readEntity(TrelloCardsSearchResponse::class.java).cards.toList()
    }

    fun getBoards(boardIds: List<String>): List<TrelloBoard> =
        boardIds.map { boardId ->
            getTarget()
                    .path("boards")
                    .path(boardId)
                    .request()
                    .get()
        }
        .map { response->
            if(response.status != 200) {
                throw RuntimeException("Error reading Trello board: ${response.readEntity(String::class.java)}")
            }
//            println(response.readEntity(String::class.java))
            response.readEntity(TrelloBoard::class.java)
        }

    fun loadLists(listsIds: List<String>): List<TrelloList> = listsIds.map { listId ->
        getTarget()
                .path("lists")
                .path(listId)
                .request()
                .get()
        }
        .map { response->
            if(response.status != 200) {
                throw RuntimeException("Error reading Trello list: ${response.readEntity(String::class.java)}")
            }
            response.readEntity(TrelloList::class.java)
        }

    private fun getTarget() = WebTargetFactory(trelloConfig.baseUrl)
            .getTarget("/1")
            .queryParam("key", trelloConfig.key)
            .queryParam("token", trelloConfig.token)

    private fun getQuery(cardsFilter: TrelloCardsFilter, trelloConfig: BVTrelloConfig): String =
            "@${getUser(cardsFilter.user, trelloConfig.sourceName)}" +
                    (cardsFilter.listName?.let { " list:\"${cardsFilter.listName}\"" } ?: "") +
                    " edited:${getDaysBackFromNow(cardsFilter.since)}" +
                    " sort:edited"

    private fun getUser(userAlias: String?, sourceName:String): String =
            if (userAlias == null) "me"
            else usersConfigProvider.getUserName(userAlias, sourceName)

    private fun getDaysBackFromNow(since: ZonedDateTime): Int =
            ChronoUnit.DAYS.between(since, ZonedDateTime.now()).toInt()

}