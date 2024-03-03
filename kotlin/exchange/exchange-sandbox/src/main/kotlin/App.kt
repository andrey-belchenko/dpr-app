package cc.datafabric.exchange.app.con.rabbit.incoming

import cc.datafabric.exchange.cim.repository.impl.PlatformDirectRepository
import cc.datafabric.exchange.scenario.model.data.Substation
import cc.datafabric.exchange.cim.repository.common.query.Query
import cc.datafabric.exchange.cim.utils.DataSetMapper

object App {
    fun main() {
        val id = "Substation_94e0c02b-dcf9-4673-bfa3-7b32a2acaddf"
        val query = Query()
            .filterById(id)
            .include(Substation::infSupplyCenter)

        val repo =  PlatformDirectRepository()
        val repoDs= repo.executeQueries(listOf(query))
        println(repoDs.toJson())


        val dataSet = DataSetMapper.toDataSet(repoDs)
        val substation = dataSet.get(id) as Substation
        println(substation.name)
    }
}