package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import java.util.Optional
import java.util.Properties

@ConfigMapping(prefix = "deployment")
interface DeploymentConfig {

    @WithDefault("0 0 1 ? * * *")
    fun cleanup(): String

    fun configurationIri(): Optional<String>

    fun outputDirectory(): String

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["deployment.cleanup"] = cleanup()
            props["deployment.configurationIri"] = configurationIri().orElse("")
            props["deployment.outputDirectory"] = if (outputDirectory() == "tmp") System.getProperty("java.io.tmpdir") else outputDirectory()
        }
    }
}