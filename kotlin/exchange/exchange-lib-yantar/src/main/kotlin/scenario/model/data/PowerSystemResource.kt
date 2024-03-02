//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class PowerSystemResource : IdentifiedObject() {
    val Assets: Links<Asset> by LinksDelegate(inverseProperty = Asset::PowerSystemResources)
    var ccsCode: String? by ValueDelegate()
}