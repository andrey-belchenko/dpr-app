//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.ConductingEquipment as ConductingEquipmentClass
import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class BaseVoltage : IdentifiedObject() {
    val ConductingEquipment: Links<ConductingEquipmentClass> by LinksDelegate(inverseProperty = ConductingEquipmentClass::BaseVoltage)
    var isDC: Boolean? by ValueDelegate()
    var nominalVoltage: Float? by ValueDelegate()
}