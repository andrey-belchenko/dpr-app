package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import java.util.*

@ConfigMapping(prefix = "extensions")
interface ExtensionsConfig {

    fun configFile(): Optional<String>

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["extensions.configFile"] = configFile().orElse("")
        }
    }
}