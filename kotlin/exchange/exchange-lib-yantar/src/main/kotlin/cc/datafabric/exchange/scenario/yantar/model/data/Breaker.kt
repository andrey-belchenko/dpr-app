//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Breaker : ProtectedSwitch() {
    var inTransitTime: Float? by ValueDelegate()
}