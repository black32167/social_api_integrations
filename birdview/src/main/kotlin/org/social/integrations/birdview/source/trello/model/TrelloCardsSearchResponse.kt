package org.social.integrations.birdview.source.trello.model

class TrelloCardsSearchResponse (
        val cards: Array<TrelloCard>
)

class TrelloCard (
        val id: String,
        val idBoard: String,
        val dateLastActivity: String,
        val name: String,
        val desc: String,
        val url: String,
        val labels: Array<TrelloCardLabel>,
        val idList: String
)

class TrelloCardLabel(
        val id: String,
        val name: String
)

