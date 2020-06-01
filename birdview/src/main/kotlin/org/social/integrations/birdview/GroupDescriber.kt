package org.social.integrations.birdview

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.BVDocumentId
import org.social.integrations.birdview.resolve.GroupIdResolver
import javax.inject.Named

@Named
class GroupDescriber(
        private val groupResolver: GroupIdResolver
) {
    fun describe(groups: List<BVDocument>) {
        val groupDescriptions:Map<BVDocumentId, String> = groupResolver.describe(groups.flatMap { it.ids })
        groups
                .filter { it.title == null }
                .forEach { group ->
                    group.title = group.ids.firstOrNull()
                            ?.let { docId -> groupDescriptions[docId] }
                }
    }
}