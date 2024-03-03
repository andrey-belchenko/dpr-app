//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class ACLineSegment : Conductor() {
    val LineSpans: Links<LineSpan> by LinksDelegate(inverseProperty = LineSpan::ACLineSegment)
    var StartTower: Tower? by LinkDelegate(inverseProperty = Tower::StartACLineSegment)
    var EndTower: Tower? by LinkDelegate(inverseProperty = Tower::EndACLineSegment)
    var g0ch: Float? by ValueDelegate()
    var x0: Float? by ValueDelegate()
    var gch: Float? by ValueDelegate()
    var x: Float? by ValueDelegate()
    var r: Float? by ValueDelegate()
    var b0ch: Float? by ValueDelegate()
    var r0: Float? by ValueDelegate()
    var isTap: Boolean? by ValueDelegate()
    var bch: Float? by ValueDelegate()
}