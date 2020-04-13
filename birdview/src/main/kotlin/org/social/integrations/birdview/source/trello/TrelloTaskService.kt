package org.social.integrations.birdview.source.trello

import org.social.integrations.birdview.source.trello.model.TrelloCardsSearchResponse
import org.social.integrations.tools.WebTargetFactory
import social.api.task.model.Task
import social.api.task.model.Tasks
import social.api.task.server.TaskApiService
import javax.inject.Named

@Named
class TrelloTaskService(
        trelloConfigProvider: BVTrelloConfigProvider
) : TaskApiService {
    private val maxResults = 10;
    private val trelloConfig = trelloConfigProvider.getTrello()
    private val trelloRestTarget = WebTargetFactory(trelloConfig.baseUrl)
            .getTarget("/1")
            .queryParam("key", trelloConfig.key)
            .queryParam("token", trelloConfig.token)

    override fun getTask(p0: String?): Task {
        TODO("Not yet implemented")
    }

    override fun createTask(p0: Task?): Task {
        TODO("Not yet implemented")
    }

    override fun getTasks(status: String): Tasks {
        val listName = status
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

        val tasks = trelloCardsContainer.cards.map { card -> Task().apply {
            id = card.id
            title = card.name
            updated = card.dateLastActivity
            httpUrl = card.url
        } }
        return Tasks().apply { setTasks(tasks) }
    }
}