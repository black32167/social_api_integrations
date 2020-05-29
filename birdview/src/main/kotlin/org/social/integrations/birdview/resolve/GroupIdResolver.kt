package org.social.integrations.birdview.resolve

import org.social.integrations.birdview.analysis.DocumentGroupId
import org.social.integrations.birdview.config.BVJiraConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.config.BVTrelloConfig
import org.social.integrations.birdview.source.jira.JiraClientProvider
import org.social.integrations.birdview.source.trello.TrelloClientProvider
import org.social.integrations.birdview.source.trello.TrelloTaskService
import javax.inject.Named

@Named
class GroupIdResolver(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val jiraClientProvider: JiraClientProvider,
        private val trelloClientProvider: TrelloClientProvider

) {
    fun describe(docIds: Collection<DocumentGroupId>): Map<DocumentGroupId, String> = docIds
            .groupBy { it.sourceName }.entries
            .map { (source, ids) -> describeIds(source, ids) }
            .fold(mutableMapOf<DocumentGroupId, String>()) { acc, map -> acc.putAll(map); acc }

    private fun describeIds(sourceName: String, groupIds: List<DocumentGroupId>) =
            sourcesConfigProvider.getConfigByName(sourceName)?.let { sourceConfig ->
                when (sourceConfig) {
                    is BVJiraConfig -> describeJiraId(sourceConfig, groupIds)
                    is BVTrelloConfig -> describeTrelloIds(sourceConfig, groupIds)
                    else -> null
                }
            } ?: mapOf()

    private fun describeJiraId(sourceConfig: BVJiraConfig, groupIds: List<DocumentGroupId>): Map<DocumentGroupId, String> {
        val issuesMap = jiraClientProvider
                .getJiraClient(sourceConfig)
                .loadIssues(groupIds.map { it.id })
                .associateBy { it.key }
        return groupIds.map { docGId -> docGId to (issuesMap[docGId.id]?.fields?.summary ?: "") }.toMap()
    }

    private fun describeTrelloIds(sourceConfig: BVTrelloConfig, groupIds: List<DocumentGroupId>): Map<DocumentGroupId, String> =
            groupIds
                    .groupBy { it.type }.entries
                    .map { (type, docIds) ->
                        when(type) {
                            TrelloTaskService.TRELLO_BOARD_TYPE -> describeTrelloBoardId(docIds, sourceConfig)
                            else -> mapOf<DocumentGroupId, String>()
                        }
                    }
                    .fold(mutableMapOf<DocumentGroupId, String>()) { acc, map -> acc.putAll(map); acc }

    private fun describeTrelloBoardId(boardIds: List<DocumentGroupId>, sourceConfig: BVTrelloConfig): Map<DocumentGroupId, String> {
        val boardsMap = trelloClientProvider
                .getTrelloClient(sourceConfig)
                .getBoards(boardIds.map { it.id })
                .associateBy { board->board.id }
        return boardIds.associateWith { boardsMap.get(it.id)?.name ?: "???" }
    }

}