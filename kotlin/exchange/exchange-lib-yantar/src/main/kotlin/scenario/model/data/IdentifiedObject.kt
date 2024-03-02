//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.ModelObject
import cc.datafabric.linesapp.sys.model.LinkDelegate
import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class IdentifiedObject : ModelObject() {
    var ParentObject: IdentifiedObject? by LinkDelegate()
    var name: String? by ValueDelegate()
    var aliasName: String? by ValueDelegate()
    var mRID: String? by ValueDelegate()
    var description: String? by ValueDelegate()
}