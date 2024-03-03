//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.exchange.cim.model.LinkDelegate

@Suppress("PropertyName", "unused")
open class Line : EquipmentContainer() {
    val LineSpans: Links<LineSpan> by LinksDelegate(inverseProperty = LineSpan::Line)
    var Region: SubGeographicalRegion? by LinkDelegate(inverseProperty = SubGeographicalRegion::Lines)
    val AccountPartLines: Links<AccountPartLine> by LinksDelegate()
    val Towers: Links<Tower> by LinksDelegate()
}