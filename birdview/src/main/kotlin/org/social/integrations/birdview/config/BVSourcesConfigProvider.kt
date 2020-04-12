package org.social.integrations.birdview.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import java.nio.file.Path
import javax.inject.Named

@Named
class BVSourcesConfigProvider(@Value("\${config.location}") val sourcesConfigFile:Path) {
     val objectMapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(KotlinModule())
    fun getConfig(): BVSourcesConfig
            = objectMapper.readValue(sourcesConfigFile.toFile(), BVSourcesConfig::class.java)
}

class BVSourcesConfig (
    val jira:BVJiraConfig
)

class BVJiraConfig (
        val baseUrl: String,
        val user: String,
        val token: String
)