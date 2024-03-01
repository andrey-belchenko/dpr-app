package cc.datafabric.adapter.consumer

import cc.datafabric.adapter.config.ExchangeConfig
import cc.datafabric.adapter.config.MongoConfig
import cc.datafabric.adapter.extension.ExtensionFactoryRepository
import cc.datafabric.core.Profile
import cc.datafabric.core.classByLocalName
import cc.datafabric.extensions.PipelineExtension
import cc.datafabric.extensions.api.ServicesAwareExtension
import cc.datafabric.services.ConfigurationService
import cc.datafabric.services.LogService
import cc.datafabric.services.ObjectsService
import cc.datafabric.services.ProfilesService
import cc.datafabric.services.SemqueryService
import cc.datafabric.services.TasksService
import cc.datafabric.services.UtilitiesService
import cc.datafabric.services.VocabulariesService
import io.quarkus.runtime.Startup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.Response
import kotlin.system.measureTimeMillis

@Singleton
@Startup
@Path("/debug")
class ExtensionConsumer(
    private val configurationService: ConfigurationService,
    private val logService: LogService,
    private val objectsService: ObjectsService,
    private val profilesService: ProfilesService,
    private val semQueryService: SemqueryService,
    private val tasksService: TasksService,
    private val utilitiesService: UtilitiesService,
    private val vocabulariesService: VocabulariesService,
    private val extensionFactoryRepository: ExtensionFactoryRepository,
    private val mongoConfig: MongoConfig,
    private val exchangeConfig: ExchangeConfig,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ExtensionConsumer::class.java)
        private var profile:Profile? = null
    }

    /* Enable this code if you want to load extension through the extensions mechanism
       Also, ensure you have removed the dependency on "extensions-test" in build.gradle, if enabled

        private val pipelineExtension: ServicesAwareExtension by lazy {
            val factories = extensionFactoryRepository.getFactories()
            val pipelineExtensionFactory = factories.first()
            pipelineExtensionFactory.createExtension(
                mongoConfig.toProperties(),
                configurationService,
                logService,
                objectsService,
                profilesService,
                semQueryService,
                tasksService,
                utilitiesService,
                vocabulariesService,
            )
        }
     */



    private val pipelineExtension = PipelineExtension(
        exchangeConfig.toProperties(),
        configurationService,
        logService,
        objectsService,
        profilesService,
        semQueryService,
        tasksService,
        utilitiesService,
        vocabulariesService,
    )

    @POST
    fun handleMessage(message: String): Response {
        val result = pipelineExtension.handle(message, mapOf())
        // Summary и OperationLog возвращаем в качестве ответа
        val responseText =  """{
                "summary":${result["Summary"]}, 
                "operationLog":${result["OperationLog"]}, 
            }""".trimIndent()
        // Остальную информацию пишем в лог
        logger.info("\nDiff for platform:\n${result["Platform"]}")
        logger.info("\nNew key mappings for platform:\n${result["KeyMapping"]}")
        logger.info("\nDiff for SK-11:\n${result["Sk11"]}")
        logger.info("\nMessages to bus:\n${result["Bus"]}")
        return Response.ok(responseText).build()
    }







}