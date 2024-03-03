//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.LineSpan as LineSpanClass
import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Switch : ConductingEquipment() {
    var LineSpan: LineSpanClass? by LinkDelegate(inverseProperty = LineSpanClass::Switches)
    var tower: Tower? by LinkDelegate(inverseProperty = Tower::switches)
    var normalOpen: Boolean? by ValueDelegate()
    var retained: Boolean? by ValueDelegate()
    var differenceInTransitTime: Float? by ValueDelegate()
    var ratedCurrent: Float? by ValueDelegate()
}