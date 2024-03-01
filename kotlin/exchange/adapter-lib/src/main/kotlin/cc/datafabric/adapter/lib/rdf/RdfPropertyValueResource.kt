package cc.datafabric.adapter.lib.rdf


import cc.datafabric.adapter.lib.data.Namespace
import cc.datafabric.adapter.lib.data.DataClass
import cc.datafabric.adapter.lib.data.IDataPropertyValueResource
import org.apache.jena.rdf.model.Statement

class RdfPropertyValueResource(override val entity: RdfEntity, private val statement: Statement) :
    IDataPropertyValueResource, RdfPropertyValue(entity, statement)  {
    override fun getValueText():String {
//      return  getObject().asResource().localName //обрезает цифры в начале
        return   getObject().asResource().uri.split("#").last()
    }

    fun getValueNamespace(): Namespace {
        val iri = getObject().asResource().nameSpace
        val prefix =  statement.model.getNsURIPrefix(iri)
        return Namespace(prefix,iri)
    }

//    fun getValueUri():String{
//       return  getObject().asResource().uri
//    }

   override fun getValueClass(): DataClass? {
      return  entity.model.getEntity(getValueText())?.getClass()
    }

    override fun isReference():Boolean{
        return true
    }


}