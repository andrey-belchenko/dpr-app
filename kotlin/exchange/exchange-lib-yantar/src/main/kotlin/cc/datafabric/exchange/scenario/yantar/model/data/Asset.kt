//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Asset : IdentifiedObject() {
    val PowerSystemResources: Links<PowerSystemResource> by LinksDelegate(inverseProperty = PowerSystemResource::Assets)
    var SerialNumber: String? by ValueDelegate()
    var position: String? by ValueDelegate()
    var type: String? by ValueDelegate()
}