package cc.datafabric.exchange.app.api.yantar

import cc.datafabric.exchange.cim.repository.common.Repository
import cc.datafabric.exchange.scenario.model.data.Substation
import cc.datafabric.exchange.cim.repository.common.query.Query
import cc.datafabric.exchange.cim.utils.DataSetMapper
import io.quarkus.runtime.Startup
import view.data.PowerCenter
import javax.inject.Inject
import javax.inject.Singleton
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
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
        return Response.ok("ok").build()
    }

    @GET
    @Path("/powerCenters/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPowerCenter(@PathParam("id") id: String): PowerCenter {
        val query = Query()
            .filterById(id)
            .include(Substation::infSupplyCenter)
        val repoDs = repository.executeQueries(listOf(query))
        val dataSet = DataSetMapper.toDataSet(repoDs)
        val substation = dataSet.get(id) as? Substation ?: throw NotFoundException()
        return PowerCenter(
            id = substation.id,
            name = substation.name,
            aliasName = substation.aliasName,
            yearOfPlugin = substation.infSupplyCenter?.yearOfPlugin
        )
    }

}








