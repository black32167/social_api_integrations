package org.social.integrations.birdview.source.trello.model

class TrelloCardsSearchResponse (
        val cards: Array<TrelloCard>
)

class TrelloCard (
        val id: String,
        val dateLastActivity: String,
        val name: String,
        val url: String,
        val labels: Array<TrelloCardLabel>
)

class TrelloCardLabel(
        val id: String,
        val name: String
)