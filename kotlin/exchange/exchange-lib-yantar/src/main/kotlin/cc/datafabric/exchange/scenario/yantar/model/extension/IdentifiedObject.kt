package cc.datafabric.linesapp.scenario.model.extension

import cc.datafabric.linesapp.scenario.model.data.IdentifiedObject

var IdentifiedObject.affected: Boolean
    get() = (this.getExtraProperty(IdentifiedObject::affected.name) ?: false) as Boolean
    set(value) = this.setExtraProperty(IdentifiedObject::affected.name,value)

var IdentifiedObject.updated: Boolean
    get() = (this.getExtraProperty(IdentifiedObject::updated.name) ?: false) as Boolean
    set(value) = this.setExtraProperty(IdentifiedObject::updated.name,value)