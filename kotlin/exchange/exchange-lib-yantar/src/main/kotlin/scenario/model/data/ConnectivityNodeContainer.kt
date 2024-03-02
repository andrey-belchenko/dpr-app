//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.Links
import cc.datafabric.linesapp.sys.model.LinksDelegate

@Suppress("PropertyName", "unused")
open class ConnectivityNodeContainer : PowerSystemResource() {
    val ConnectivityNodes: Links<ConnectivityNode> by LinksDelegate(inverseProperty = ConnectivityNode::ConnectivityNodeContainer)
}