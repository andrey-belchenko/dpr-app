//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class ProtectedSwitch : Switch() {
    var breakingTime: Float? by ValueDelegate()
    var breakingCapacity: Float? by ValueDelegate()
}