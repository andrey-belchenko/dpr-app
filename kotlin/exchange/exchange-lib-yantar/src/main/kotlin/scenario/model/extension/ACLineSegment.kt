package cc.datafabric.linesapp.scenario.model.extension

import cc.datafabric.linesapp.scenario.model.data.ACLineSegment
import cc.datafabric.linesapp.scenario.model.data.LineSpan

var ACLineSegment.firstLineSpan: LineSpan?
    get() = this.getExtraProperty(ACLineSegment::firstLineSpan.name) as LineSpan?
    set(value) = this.setExtraProperty(ACLineSegment::firstLineSpan.name,value)

var ACLineSegment.lastLineSpan: LineSpan?
    get() = this.getExtraProperty(ACLineSegment::lastLineSpan.name) as LineSpan?
    set(value) = this.setExtraProperty(ACLineSegment::lastLineSpan.name,value)