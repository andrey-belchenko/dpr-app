package cc.datafabric.linesapp.scenario.model.extension

import cc.datafabric.linesapp.scenario.model.data.ACLineSegment
import cc.datafabric.linesapp.scenario.model.data.IdentifiedObject
import cc.datafabric.linesapp.scenario.model.data.LineSpan

var LineSpan.isLast: Boolean
    get() = (this.getExtraProperty(LineSpan::isLast.name) ?: false) as Boolean
    set(value) = this.setExtraProperty(LineSpan::isLast.name,value)