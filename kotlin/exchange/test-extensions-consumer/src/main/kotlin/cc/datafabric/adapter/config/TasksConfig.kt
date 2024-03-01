package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

@ConfigMapping(prefix = "tasks")
interface TasksConfig {
    /**
     * The root directory where the result files will be stored.
     */
    fun rootDirectory(): String

    /**
     * The directory to store tasks as json-files to persist between application running.
     */
    fun storeDirectory(): String

    /**
     * For each task.
     */
    fun maxDurationInMills(): Long

    /**
     * For all tasks.
     */
    fun cacheExpirationTimeoutInMills(): Long

    /**
     * Fixed number of coroutines to avoid SQL connection leaks.
     */
    fun coroutineParallelism(): Int

    /**
     * the number of entities or links per butch operation (`upsertGraphEntities` or `createGraphLinks`)
     */
    @WithDefault("100")
    fun fullImportChunkSize(): Int

    fun toProperties(): Properties {
        return Properties().also { props ->
            props["rootDirectory"] = rootDirectoryPath().toString()
            props["storeDirectory"] = storeDirectoryPath().toString()
            props["maxDurationInMills"] = maxDurationInMills()
            props["cacheExpirationTimeoutInMills"] = cacheExpirationTimeoutInMills()
            props["coroutineParallelism"] = coroutineParallelism()
            props["fullImportChunkSize"] = fullImportChunkSize()
        }
    }
}

fun TasksConfig.storeDirectoryPath(): Path = prepareDirectory(storeDirectory())

fun TasksConfig.rootDirectoryPath(): Path = prepareDirectory(rootDirectory())

private fun prepareDirectory(configDir: String): Path =
    Paths.get(configDir.takeIf { it.isNotBlank() && it != "tmp" } ?: System.getProperty("java.io.tmpdir"))
        .toAbsolutePath()
