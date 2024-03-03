//generated from profile
package cc.datafabric.exchange.scenario.model.data

import cc.datafabric.exchange.cim.model.ModelObject
import cc.datafabric.exchange.cim.model.Links
import cc.datafabric.exchange.cim.model.LinksDelegate
import cc.datafabric.exchange.cim.model.LinkDelegate
import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class IdentifiedObject : ModelObject() {
    val ChildObjects: Links<IdentifiedObject> by LinksDelegate(inverseProperty = IdentifiedObject::ParentObject)
    var ParentObject: IdentifiedObject? by LinkDelegate(inverseProperty = IdentifiedObject::ChildObjects)
    var name: String? by ValueDelegate()
    var aliasName: String? by ValueDelegate()
    var mRID: String? by ValueDelegate()
    var description: String? by ValueDelegate()
}