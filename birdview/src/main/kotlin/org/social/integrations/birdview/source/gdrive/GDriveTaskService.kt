package org.social.integrations.birdview.source.gdrive

import org.social.integrations.birdview.analysis.BVDocument
import org.social.integrations.birdview.analysis.BVDocumentId
import org.social.integrations.birdview.config.BVGoogleConfig
import org.social.integrations.birdview.config.BVSourcesConfigProvider
import org.social.integrations.birdview.request.TasksRequest
import org.social.integrations.birdview.source.BVTaskSource
import org.social.integrations.birdview.source.gdrive.model.GDriveFile
import java.util.*
import javax.inject.Named

@Named
class GDriveTaskService(
        private val clientProvider: GDriveClientProvider,
        private val bvConfigProvider: BVSourcesConfigProvider
) : BVTaskSource {
    companion object {
        val GDRIVE_FILE_TYPE = "gDriveFile"
    }
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    override fun getTasks(request: TasksRequest): List<BVDocument> =
            bvConfigProvider.getConfigOfType(BVGoogleConfig::class.java)
                    ?.let { config ->
                        clientProvider.getGoogleApiClient(config)
                                .getFiles(request)
                                .files
                                .map { file -> toBVDocument(file, config) }
                    }
                    ?:emptyList()

    override fun getType() = "gdrive"

    private fun toBVDocument(file: GDriveFile, config: BVGoogleConfig) =
            BVDocument(
                        ids = setOf(BVDocumentId(id = file.id, type = GDRIVE_FILE_TYPE, sourceName = config.sourceName)),
                       // body = file.name,
                        title = file.name,
                        updated = parseDate(file.modifiedTime))

    private fun parseDate(dateString: String): Date = dateTimeFormat.parse(dateString)
}