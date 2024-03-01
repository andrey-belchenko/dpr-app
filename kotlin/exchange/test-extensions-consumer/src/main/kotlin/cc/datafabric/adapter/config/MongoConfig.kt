package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import java.util.Properties


@ConfigMapping(prefix = "mongo")
interface MongoConfig {

    fun url(): String

    fun database(): String

    fun socketConnectTimeoutMsec(): Int

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["mongo.url"] = url()
            props["mongo.database"] = database()
            props["mongo.socketConnectTimeoutMsec"] = socketConnectTimeoutMsec()
        }
    }
}