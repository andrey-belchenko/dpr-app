//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.ConductingEquipment as ConductingEquipmentClass
import cc.datafabric.linesapp.sys.model.LinkDelegate
import cc.datafabric.linesapp.scenario.model.data.ConnectivityNode as ConnectivityNodeClass

@Suppress("PropertyName", "unused")
open class Terminal : ACDCTerminal() {
    var ConductingEquipment: ConductingEquipmentClass? by LinkDelegate(inverseProperty = ConductingEquipmentClass::Terminals)
    var ConnectivityNode: ConnectivityNodeClass? by LinkDelegate(inverseProperty = ConnectivityNodeClass::Terminals)
}