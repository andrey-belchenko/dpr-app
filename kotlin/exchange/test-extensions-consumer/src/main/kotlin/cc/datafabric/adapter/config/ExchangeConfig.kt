package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import java.util.Properties


@ConfigMapping(prefix = "exchange")
interface ExchangeConfig {

    fun mongoUrl(): String

    fun mongoDatabase(): String

    fun workDir(): String

    fun profileVersion(): String

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["mongo-url"] = mongoUrl()
            props["mongo-database"] = mongoDatabase()
            props["work-dir"] = workDir()
            props["profile-version"] = profileVersion()
        }
    }
}