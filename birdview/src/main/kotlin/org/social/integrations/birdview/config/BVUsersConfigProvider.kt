package org.social.integrations.birdview.config

import org.social.integrations.birdview.utils.JsonDeserializer
import javax.inject.Named

@Named
class BVUsersConfigProvider(
        private val bvRuntimeConfig: BVRuntimeConfig,
        private val jsonDeserializer: JsonDeserializer
) {
    fun getUserName(userAlias: String, sourceName: String): String? =
            getConfig()
                    .find { it.sourceName == sourceName }
                    ?.users
                    ?.find { it.alias == userAlias }
                    ?.sourceUserName

    private fun getConfig(): Array<UserSourcesConfig> = try {
        jsonDeserializer.deserialize(bvRuntimeConfig.sourcesConfigFileName)
    } catch (e: Exception) {
        arrayOf()
    }
}

class UserSourcesConfig(
        val sourceName: String,
        val users: Array<BVUserConfig>
)

class BVUserConfig (
        val alias: String,
        val sourceUserName: String
)