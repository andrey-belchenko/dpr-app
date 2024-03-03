package cc.datafabric.exchange.app.api.yantar

import cc.datafabric.exchange.cim.repository.common.Repository
import cc.datafabric.exchange.scenario.yantar.model.data.Substation
import cc.datafabric.linesapp.sys.repository.common.query.Query
import io.quarkus.runtime.Startup
import javax.inject.Inject
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response

@Singleton
@Startup
@Path("/")
class Main {

    @Inject
    lateinit var repository: Repository
    @GET
    @Path("test")
    fun getScriptParams(): Response {

        val query = Query()
            .filterById("Substation_94e0c02b-dcf9-4673-bfa3-7b32a2acaddf")
            .include(Substation::infSupplyCenter)
        val repoDs= repository.executeQueries(listOf(query))
        val result = repoDs.toJson()
        return Response.ok(result).build()
    }
}








