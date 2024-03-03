//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.scenario.model.data.ConnectivityNodeContainer as ConnectivityNodeContainerClass
import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate

@Suppress("PropertyName", "unused")
open class ConnectivityNode : IdentifiedObject() {
    var ConnectivityNodeContainer: ConnectivityNodeContainerClass? by LinkDelegate(inverseProperty = ConnectivityNodeContainerClass::ConnectivityNodes)
    val Terminals: Links<Terminal> by LinksDelegate(inverseProperty = Terminal::ConnectivityNode)
}