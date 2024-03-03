//generated from profile
package cc.datafabric.exchange.scenario.yantar.model.data

import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Substation : EquipmentContainer() {
    var infSupplyCenter: InfSupplyCenter? by LinkDelegate(inverseProperty = InfSupplyCenter::substation)
    var isSupplyCenter: Boolean? by ValueDelegate()
}