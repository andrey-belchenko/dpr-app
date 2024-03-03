import cc.datafabric.exchange.cim.repository.common.Repository
import cc.datafabric.exchange.cim.repository.impl.PlatformDirectRepository
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces

@Suppress("unused")
@ApplicationScoped
class Producer {

    @Produces
    fun repository(): Repository {
        return PlatformDirectRepository()
    }
}