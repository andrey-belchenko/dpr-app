//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.AccountPartLine as AccountPartLineClass
import cc.datafabric.linesapp.sys.model.LinkDelegate
import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Tower : Structure() {
    var AccountPartLine: AccountPartLineClass? by LinkDelegate(inverseProperty = AccountPartLineClass::Towers)
    var EndACLineSegment: ACLineSegment? by LinkDelegate(inverseProperty = ACLineSegment::EndTower)
    val firstTower: Links<AccountPartLineClass> by LinksDelegate(inverseProperty = AccountPartLineClass::firstTower)
    val StartTower: Links<LineSpan> by LinksDelegate(inverseProperty = LineSpan::StartTower)
    val lastTower: Links<AccountPartLineClass> by LinksDelegate(inverseProperty = AccountPartLineClass::lastTower)
    var EndTower: LineSpan? by LinkDelegate(inverseProperty = LineSpan::EndTower)
    val StartACLineSegment: Links<ACLineSegment> by LinksDelegate(inverseProperty = ACLineSegment::StartTower)
    val switches: Links<Switch> by LinksDelegate(inverseProperty = Switch::tower)
    var groundingMode: String? by ValueDelegate()
}