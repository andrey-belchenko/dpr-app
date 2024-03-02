//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.scenario.model.data.BaseVoltage as BaseVoltageClass
import cc.datafabric.linesapp.sys.model.LinkDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class ConductingEquipment : Equipment() {
    val Terminals: Links<Terminal> by LinksDelegate(inverseProperty = Terminal::ConductingEquipment)
    var BaseVoltage: BaseVoltageClass? by LinkDelegate(inverseProperty = BaseVoltageClass::ConductingEquipment)
    var isThreePhaseEquipment: Boolean? by ValueDelegate()
}