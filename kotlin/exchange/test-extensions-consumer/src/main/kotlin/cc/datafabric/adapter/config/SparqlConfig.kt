package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import java.util.Optional
import java.util.Properties

/**
 * Describes either blaze-graph remote database things or ones stored in a dataset file.
 */
@ConfigMapping(prefix = "sparql")
interface SparqlConfig {
    fun endpoint(): Optional<String>

    fun updatepoint(): Optional<String>

    fun uploadpoint(): Optional<String>

    fun datasetFile(): Optional<String>

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["endpoint"] = endpoint().orElse("")
            props["updatepoint"] = updatepoint().orElse("")
            props["uploadpoint"] = uploadpoint().orElse("")
            props["datasetFile"] = datasetFile().orElse("")
        }
    }
}
