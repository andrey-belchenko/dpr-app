//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.linesapp.sys.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class ProtectedSwitch : Switch() {
    var breakingTime: Float? by ValueDelegate()
    var breakingCapacity: Float? by ValueDelegate()
}