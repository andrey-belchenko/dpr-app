package cc.datafabric.adapter.bean_producers

import cc.datafabric.adapter.config.DatabaseConfig
import cc.datafabric.adapter.config.DeploymentConfig
import cc.datafabric.adapter.config.SparqlConfig
import cc.datafabric.adapter.config.TasksConfig
import cc.datafabric.adapter.config.storeDirectoryPath
import cc.datafabric.common.utils.Collections.Companion.requireSingle
import cc.datafabric.common.utils.RdfIOUtils
import cc.datafabric.core.PlatformConfig
import cc.datafabric.datalayer.http.listeners.KafkaNotifyingVocabularyServiceListener
import cc.datafabric.datalayer.http.listeners.NoOpVocabularyServiceListener
import cc.datafabric.datalayer.tools.tasks.TaskJob
import cc.datafabric.datalayer.tools.tasks.TaskManager
import cc.datafabric.datalayer.tools.tasks.deleteAllFiles
import cc.datafabric.datalayer.tools.tasks.deleteJson
import cc.datafabric.datalayer.tools.tasks.loadTasksFromDirectory
import cc.datafabric.datalayer.tools.tasks.plus
import cc.datafabric.errors.requireIsNonNull
import cc.datafabric.logs.AuditLogMessage
import cc.datafabric.logs.AuditLoggerFactory
import cc.datafabric.services.ConfigurationService
import cc.datafabric.services.HistoryService
import cc.datafabric.services.IamConfigService
import cc.datafabric.services.LogService
import cc.datafabric.services.ObjectsService
import cc.datafabric.services.ProfilesService
import cc.datafabric.services.PropertiesService
import cc.datafabric.services.PropertyKeys
import cc.datafabric.services.SemqueryService
import cc.datafabric.services.SystemDataService
import cc.datafabric.services.TasksService
import cc.datafabric.services.TreesService
import cc.datafabric.services.UtilitiesService
import cc.datafabric.services.VocabulariesService
import cc.datafabric.services.db.DbDataSource
import io.quarkus.arc.DefaultBean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apache.jena.riot.Lang
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance
import javax.enterprise.inject.Produces
import javax.inject.Singleton

class ServiceBeanProducer {

    companion object {
        private val logger = LoggerFactory.getLogger(ServiceBeanProducer::class.java)
    }

    @DefaultBean
    @Singleton // use single database source per application run
    fun dataLayerSource(config: DatabaseConfig): DbDataSource {
        return DbDataSource(config.toProperties())
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun historyService(source: DbDataSource): HistoryService {
        logger.info("::: init history service")
        return HistoryService(source)
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun objectsService(
        source: DbDataSource,
        profileService: ProfilesService,
        iamConfigService: IamConfigService
    ): ObjectsService {
        logger.info("::: init objects service")
        return ObjectsService(source, profileService, iamConfigService)
    }

    @DefaultBean
    @Produces
    @Singleton // has a state (cache)
    fun iamConfigService(systemDataService: SystemDataService, profileService: ProfilesService): IamConfigService {
        logger.info("::: init iam-config service")
        return IamConfigService(profileService, systemDataService)
    }

    @DefaultBean
    @Produces
    @Singleton // singleton, since profile service has a state (cache)
    fun profilesService(
        config: SparqlConfig,
        platformConfig: PlatformConfig
    ): ProfilesService {
        logger.info("::: init profiles service")
        return ProfilesService(config.toProperties(), platformConfig)
    }

    @DefaultBean
    @Produces
    @Singleton // tasks service has a state
    fun tasksService(
        tasksConfig: TasksConfig,
        sparqlConfig: SparqlConfig,
        platformConfig: PlatformConfig,
        dataLayerSource: DbDataSource,
        profileService: ProfilesService,
        iamConfigService: IamConfigService,
        taskManager: TaskManager,
        coroutineDispatcher: CoroutineDispatcher,
        logService: LogService,
    ): TasksService {
        logger.info("::: init tasks service")
        val mergedProperties = Properties()
        mergedProperties.putAll(tasksConfig.toProperties())
        mergedProperties.putAll(sparqlConfig.toProperties())
        mergedProperties["profileImVersionIri"] = platformConfig.imProfileVersionIri
        return TasksService(
            config = mergedProperties,
            dataLayerSource = dataLayerSource,
            profileService = profileService,
            iamConfigService = iamConfigService,
            logService = logService,
            taskManager = taskManager,
            coroutineDispatcher = coroutineDispatcher,
        )
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun treesService(source: DbDataSource, profileService: ProfilesService): TreesService {
        logger.info("::: init trees service")
        return TreesService(source, profileService)
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun configurationsService(
        platformConfig: PlatformConfig,
        publishConfig: DeploymentConfig,
        systemDataService: SystemDataService,
        profileService: ProfilesService,
        propertiesService: PropertiesService,
    ): ConfigurationService {
        logger.info("::: init configurations service")
        return ConfigurationService(
            platformConfig,
            publishConfig.toProperties(),
            systemDataService,
            profileService,
            propertiesService,
        )
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun logService(): LogService {
        logger.info("::: init log service")
        return object : LogService {
            override fun log(
                categoryId: Short,
                actionId: Short,
                actor: String,
                systemId: Short,
                triggerId: UUID?,
                status: Boolean,
                meta: Map<String, Any>,
                eventTimestamp: OffsetDateTime?
            ) {
                AuditLoggerFactory.getLogger().log(
                    AuditLogMessage(
                        categoryId = categoryId,
                        actionId = actionId,
                        actor = actor,
                        systemId = systemId,
                        triggerId = triggerId,
                        status = status,
                        meta = meta
                    )
                )
            }
        }
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun systemDataService(source: DbDataSource): SystemDataService {
        logger.info("::: init system data service")
        return SystemDataService(source)
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun propertiesService(source: DbDataSource): PropertiesService {
        logger.info("::: init properties service")
        return PropertiesService(source)
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun identifiersService(
        source: DbDataSource,
        kafkaNotifyingVocabularyServiceListener: Instance<KafkaNotifyingVocabularyServiceListener>,
        noOpVocabularyServiceListener: Instance<NoOpVocabularyServiceListener>,
    ): VocabulariesService {
        logger.info("::: init vocabularies service")
        val vocabularyServiceListener = if (kafkaNotifyingVocabularyServiceListener.isResolvable)
            kafkaNotifyingVocabularyServiceListener.get()
        else
            noOpVocabularyServiceListener.get()
        return VocabulariesService(source, vocabularyServiceListener)
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun semqueryService(
        source: DbDataSource,
        profileService: ProfilesService,
        systemDataService: SystemDataService
    ): SemqueryService {
        logger.info("::: init semquery service")
        return SemqueryService(source, profileService, systemDataService)
    }

    @DefaultBean
    @Produces
    @Singleton
    fun activePlatformConfiguration(
        deploymentConfig: DeploymentConfig,
        propertiesService: PropertiesService,
        systemDataService: SystemDataService,
    ): PlatformConfig {
        logger.info("::: fetching active Platform Configuration")
        val configurationIri = deploymentConfig.configurationIri().orElseGet {
            propertiesService.read(PropertyKeys.ACTIVE_CONFIGURATION_IRI.key).value
        }

        val systemRecordEntity =
            systemDataService.findByIris(setOf(configurationIri), configurationIri)
                .requireSingle()
        requireIsNonNull(systemRecordEntity.content) {
            "Cannot obtain the active Platform Configuration: record does not contain data"
        }
        val model = RdfIOUtils.loadModelFromString(systemRecordEntity.content!!, Lang.TURTLE)
        val platformConfig = PlatformConfig.from(model)[configurationIri]
        return requireIsNonNull(platformConfig) {
            "Cannot obtain the active Platform Configuration: record does not contain model"
        }
    }

    @DefaultBean
    @Produces
    @ApplicationScoped
    fun utilitiesService(profileService: ProfilesService, dataSource: DbDataSource): UtilitiesService {
        logger.info("::: init utilities service")
        return UtilitiesService(profileService, dataSource)
    }

    @Produces
    @DefaultBean
    @Singleton // single manager per application run
    fun taskManager(config: TasksConfig): TaskManager {
        logger.info("::: init tasks manager")
        val store = config.storeDirectoryPath()
        return TaskManager(
            expirationTimeoutInMillis = config.cacheExpirationTimeoutInMills(),
            loader = {
                logger.info("Load tasks from dir <$store>")
                loadTasksFromDirectory(store)
            },
            onEvict = { it: TaskJob -> it.deleteAllFiles() } + { it.deleteJson(store) }
        )
    }

    @Produces
    @DefaultBean
    @Singleton // single dispatcher per application run
    fun tasksCoroutineDispatcher(config: TasksConfig): CoroutineDispatcher {
        logger.info("::: init coroutine dispatcher")
        return limitedDispatcher(config.coroutineParallelism())
    }

    /**
     * Creates `CoroutineDispatcher`.
     * In order to avoid SQL-connection leaks, the number of coroutine is limited.
     * @param [parallelism][Int] number of coroutines for [dispatcher][CoroutineDispatcher]
     * @return [CoroutineDispatcher]
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun limitedDispatcher(parallelism: Int): CoroutineDispatcher {
        require(parallelism > 0) { "Illegal parallelism: $parallelism" }
        return Dispatchers.IO.limitedParallelism(parallelism = parallelism)
    }
}