package cc.datafabric.adapter.lib.rdf

import org.apache.jena.rdf.model.Statement

class RdfPropertyValueLiteral(override val entity: RdfEntity, private val statement: Statement) : RdfPropertyValue(entity, statement) {
    override fun getValueText():String {
      return getObject().asLiteral().string
    }
    override fun isReference():Boolean{
        return false
    }
}