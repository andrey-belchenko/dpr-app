package cc.datafabric.adapter.extension

import cc.datafabric.adapter.config.ExtensionsConfig
import cc.datafabric.extensions.ExtensionRegistry
import cc.datafabric.extensions.api.factory.ServicesAwareExtensionFactory
import io.quarkus.runtime.Startup
import javax.inject.Singleton

@Singleton
@Startup
class ExtensionFactoryRepository(
    private val config: ExtensionsConfig,
) {

    private val registry: ExtensionRegistry? by lazy {
        config.configFile().orElse(null)?.let{
            ExtensionRegistry(it)
        }
    }

    fun getFactories(): List<ServicesAwareExtensionFactory> {
        return if (registry != null) {
            checkNotNull(registry).listExtensions(ServicesAwareExtensionFactory::class.java)
                .map {
                    it as ServicesAwareExtensionFactory
                }
                .toList()
        } else {
            emptyList()
        }
    }
}