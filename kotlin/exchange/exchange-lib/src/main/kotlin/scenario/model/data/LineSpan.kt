//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.AccountPartLine as AccountPartLineClass
import cc.datafabric.linesapp.sys.model.LinkDelegate
import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.scenario.model.data.ACLineSegment as ACLineSegmentClass
import cc.datafabric.linesapp.scenario.model.data.Line as LineClass
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class LineSpan : IdentifiedObject() {
    var AccountPartLine: AccountPartLineClass? by LinkDelegate(inverseProperty = AccountPartLineClass::LineSpans)
    val Switches: Links<Switch> by LinksDelegate(inverseProperty = Switch::LineSpan)
    var ACLineSegment: ACLineSegmentClass? by LinkDelegate(inverseProperty = ACLineSegmentClass::LineSpans)
    var Line: LineClass? by LinkDelegate(inverseProperty = LineClass::LineSpans)
    var EndTower: Tower? by LinkDelegate(inverseProperty = Tower::EndTower)
    var StartTower: Tower? by LinkDelegate(inverseProperty = Tower::StartTower)
    var length: Float? by ValueDelegate()
    var bWireTypeName: String? by ValueDelegate()
    var isFromSubstation: Boolean? by ValueDelegate()
    var isToSubstation: Boolean? by ValueDelegate()
    var aWireTypeName: String? by ValueDelegate()
    var cWireTypeName: String? by ValueDelegate()
}