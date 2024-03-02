//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class EquipmentContainer : ConnectivityNodeContainer() {
    val AdditionalGroupedEquipment: Links<Equipment> by LinksDelegate(inverseProperty = Equipment::AdditionalEquipmentContainer)
    val Equipments: Links<Equipment> by LinksDelegate(inverseProperty = Equipment::EquipmentContainer)
    var isNotGridCompany: Boolean? by ValueDelegate()
}