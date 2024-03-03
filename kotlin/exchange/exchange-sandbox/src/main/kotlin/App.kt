package cc.datafabric.exchange.app.con.rabbit.incoming

import cc.datafabric.exchange.cim.repository.impl.PlatformDirectRepository
import cc.datafabric.exchange.scenario.yantar.model.data.Substation
import cc.datafabric.linesapp.sys.repository.common.query.Query
import kotlin.system.exitProcess

object App {
    fun main() {

        val query = Query()
            .filterById("Substation_94e0c02b-dcf9-4673-bfa3-7b32a2acaddf")
            .include(Substation::infSupplyCenter)

        val repo =  PlatformDirectRepository()
        val repoDs= repo.executeQueries(listOf(query))
        println(repoDs.toJson())
    }
}