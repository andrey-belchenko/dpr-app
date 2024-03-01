package cc.datafabric.extensions

import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.extensions.api.ServicesAwareExtension
import cc.datafabric.services.ConfigurationService
import cc.datafabric.services.LogService
import cc.datafabric.services.ObjectsService
import cc.datafabric.services.ProfilesService
import cc.datafabric.services.SemqueryService
import cc.datafabric.services.TasksService
import cc.datafabric.services.UtilitiesService
import cc.datafabric.services.VocabulariesService
import cc.datafabric.services.entities.ExportDiffTaskEntity
import cc.datafabric.services.entities.ExportDiffTaskParametersEntity
import cc.datafabric.services.entities.ExportFullTaskEntity
import cc.datafabric.services.entities.ExportFullTaskParametersEntity
import cc.datafabric.services.entities.FileDetailsEntity
import cc.datafabric.services.entities.ImportDiffTaskEntity
import cc.datafabric.services.entities.ImportDiffTaskParametersEntity
import cc.datafabric.services.entities.ImportFullTaskEntity
import cc.datafabric.services.entities.ImportFullTaskParametersEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.time.OffsetDateTime
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.fileSize

class PipelineExtension(
    private val extensionConfig: Properties,
    private val configurationService: ConfigurationService?,
    private val logService: LogService?,
    private val objectsService: ObjectsService?,
    private val profilesService: ProfilesService?,
    private val semQueryService: SemqueryService?,
    private val tasksService: TasksService?,
    private val utilitiesService: UtilitiesService?,
    private val vocabulariesService: VocabulariesService?,
) : ServicesAwareExtension {


    init {
        initialize()
    }


    private fun initialize() {

        Config.set(ConfigNames.traceEnabled,"true")
        Config.set(ConfigNames.traceDataEnabled,"true")
        Config.set(ConfigNames.pipelineLoggingEnabled,"true")
        Config.set(ConfigNames.messageLoggingEnabled,"true")
        Config.set(ConfigNames.massiveTraceEnabled,"false")
        Config.set(ConfigNames.timerTraceEnabled,"false")
        Config.set(ConfigNames.mongoUri, extensionConfig["mongo-url"].toString())
        Config.set(ConfigNames.mongoExchangeDb, extensionConfig["mongo-database"].toString())
        Config.set(ConfigNames.exchangeSettingsPath, extensionConfig["work-dir"].toString())
        Config.set(ConfigNames.exchangeBuilderPath,"")
        Config.set(ConfigNames.processorName,"exchange-extension")
        IndexManager.enable()

    }

    override fun handle(message: String, details: Map<String, Any>): Map<String, Any> {
        val execution = Execution(
            extensionConfig,
            semQueryService!!,
            vocabulariesService!!,
            profilesService!!,
            message
        )
        execution.run()
        return execution.getResult()
    }


    override fun canHandle(message: String): Boolean {
        return true
    }


}