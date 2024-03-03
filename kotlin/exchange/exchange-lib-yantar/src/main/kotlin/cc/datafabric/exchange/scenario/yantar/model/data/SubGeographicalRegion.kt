//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate

@Suppress("PropertyName", "unused")
open class SubGeographicalRegion : IdentifiedObject() {
    val Lines: Links<Line> by LinksDelegate(inverseProperty = Line::Region)
}