//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.EquipmentContainer as EquipmentContainerClass
import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Equipment : PowerSystemResource() {
    var EquipmentContainer: EquipmentContainerClass? by LinkDelegate(inverseProperty = EquipmentContainerClass::Equipments)
    val AdditionalEquipmentContainer: Links<EquipmentContainerClass> by LinksDelegate(inverseProperty = EquipmentContainerClass::AdditionalGroupedEquipment)
    var nameplate: String? by ValueDelegate()
    var isNotGridCompany: Boolean? by ValueDelegate()
    var normallyInService: Boolean? by ValueDelegate()
}