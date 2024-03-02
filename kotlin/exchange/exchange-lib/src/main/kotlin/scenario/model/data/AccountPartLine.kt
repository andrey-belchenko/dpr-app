//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.LinkDelegate
import cc.datafabric.linesapp.scenario.model.data.BaseVoltage as BaseVoltageClass
import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class AccountPartLine : IdentifiedObject() {
    var firstTower: Tower? by LinkDelegate(inverseProperty = Tower::firstTower)
    var BaseVoltage: BaseVoltageClass? by LinkDelegate()
    val Towers: Links<Tower> by LinksDelegate(inverseProperty = Tower::AccountPartLine)
    val LineSpans: Links<LineSpan> by LinksDelegate(inverseProperty = LineSpan::AccountPartLine)
    var lastTower: Tower? by LinkDelegate(inverseProperty = Tower::lastTower)
    var isTap: Boolean? by ValueDelegate()
}