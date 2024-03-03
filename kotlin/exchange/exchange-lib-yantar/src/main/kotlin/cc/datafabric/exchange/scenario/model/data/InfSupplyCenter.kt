//generated from profile
package cc.datafabric.exchange.scenario.model.data

import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class InfSupplyCenter : IdentifiedObject() {
    var substation: Substation? by LinkDelegate(inverseProperty = Substation::infSupplyCenter)
    var executedContractCount: Int? by ValueDelegate()
    var contractCount: Int? by ValueDelegate()
    var bandwidthN1: String? by ValueDelegate()
    var numberSC: String? by ValueDelegate()
    var requestCountToRedistribute: Int? by ValueDelegate()
    var requestCount: Int? by ValueDelegate()
    var yearOfPlugin: Int? by ValueDelegate()
    var yearAfterReconstruction: Int? by ValueDelegate()
}