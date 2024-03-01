package cc.datafabric.extensions.factory

import cc.datafabric.extensions.PipelineExtension
import cc.datafabric.extensions.api.factory.ServicesAwareExtensionFactory
import cc.datafabric.services.ConfigurationService
import cc.datafabric.services.LogService
import cc.datafabric.services.ObjectsService
import cc.datafabric.services.ProfilesService
import cc.datafabric.services.SemqueryService
import cc.datafabric.services.TasksService
import cc.datafabric.services.UtilitiesService
import cc.datafabric.services.VocabulariesService
import java.util.*

class PipelineExtensionFactory : ServicesAwareExtensionFactory {
    override fun createExtension(config: Properties, vararg services: Any): PipelineExtension {
        // Not optimal code, temporary solution
        val configurationService = services.asSequence().mapNotNull {
            if (it is ConfigurationService) it else null
        }.firstOrNull()
        val logService = services.asSequence().mapNotNull {
            if (it is LogService) it else null
        }.firstOrNull()
        val objectsService = services.asSequence().mapNotNull {
            if (it is ObjectsService) it else null
        }.firstOrNull()
        val profilesService = services.asSequence().mapNotNull {
            if (it is ProfilesService) it else null
        }.firstOrNull()
        val semQueryService = services.asSequence().mapNotNull {
            if (it is SemqueryService) it else null
        }.firstOrNull()
        val tasksService = services.asSequence().mapNotNull {
            if (it is TasksService) it else null
        }.firstOrNull()
        val utilitiesService = services.asSequence().mapNotNull {
            if (it is UtilitiesService) it else null
        }.firstOrNull()
        val vocabulariesService = services.asSequence().mapNotNull {
            if (it is VocabulariesService) it else null
        }.firstOrNull()

        return PipelineExtension(
            config,
            configurationService,
            logService,
            objectsService,
            profilesService,
            semQueryService,
            tasksService,
            utilitiesService,
            vocabulariesService
        )
    }
}