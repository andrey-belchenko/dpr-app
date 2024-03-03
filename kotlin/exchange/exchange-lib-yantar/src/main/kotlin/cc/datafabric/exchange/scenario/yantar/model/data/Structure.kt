//generated from profile
package cc.datafabric.linesapp.scenario.model.data

import cc.datafabric.exchange.cim.model.ValueDelegate

@Suppress("PropertyName", "unused")
open class Structure : AssetContainer() {
    var ratedVoltage: Float? by ValueDelegate()
    var rEarth: Float? by ValueDelegate()
}