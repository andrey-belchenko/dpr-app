package cc.datafabric.adapter.extension

import cc.datafabric.extensions.ExtensionRegistry
import cc.datafabric.extensions.api.ServicesAwareExtension
import cc.datafabric.adapter.config.ExtensionsConfig
import io.quarkus.runtime.Startup
import javax.inject.Singleton

@Singleton
@Startup
class ExtensionRepository(
    private val config: ExtensionsConfig,
) {

    private val registry: ExtensionRegistry? by lazy {
        config.configFile().orElse(null)?.let{
            ExtensionRegistry(it)
        }
    }

    fun getExtensions(): List<ServicesAwareExtension> {
        return if (registry != null) {
            checkNotNull(registry).listExtensions(ServicesAwareExtension::class.java)
                .map {
                    it as ServicesAwareExtension
                }
                .toList()
        } else {
            emptyList()
        }
    }
}