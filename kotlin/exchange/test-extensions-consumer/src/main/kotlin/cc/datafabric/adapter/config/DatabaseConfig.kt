package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import java.util.Optional
import java.util.Properties

@ConfigMapping(prefix = "database")
interface DatabaseConfig {
    fun url(): String

    fun user(): String

    fun password(): Optional<String>

    @WithDefault("false")
    fun ssl(): Boolean

    @WithDefault("disable")
    fun sslmode(): String

    @WithDefault("8")
    fun poolSize(): Int

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["database.url"] = url()
            props["database.user"] = user()
            props["database.password"] = password().orElse("")
            props["database.ssl"] = ssl()
            props["database.sslmode"] = sslmode()
            props["database.autocommit"] = false
            props["database.pool.size"] = poolSize()
        }
    }
}