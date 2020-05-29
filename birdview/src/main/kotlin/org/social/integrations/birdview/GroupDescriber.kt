package org.social.integrations.birdview

import org.social.integrations.birdview.analysis.DocumentGroupId
import org.social.integrations.birdview.model.BVDocumentCollection
import org.social.integrations.birdview.resolve.GroupIdResolver
import javax.inject.Named

@Named
class GroupDescriber(
        private val groupResolver: GroupIdResolver
) {
    fun describe(groups: List<BVDocumentCollection>) {
        val groupDescriptions:Map<DocumentGroupId, String> = groupResolver.describe(groups.flatMap { it.groupIds })
        groups
                .filter { it.title == null }
                .forEach { group ->
                    group.title = group.groupIds.firstOrNull()
                            ?.let { docId -> groupDescriptions[docId] }
                }
    }
}