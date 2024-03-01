package cc.datafabric.adapter.lib.rdf

import cc.datafabric.adapter.lib.data.Namespace
import cc.datafabric.adapter.lib.data.DataProperty
import cc.datafabric.adapter.lib.data.IDataPropertyValue
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Statement

abstract class RdfPropertyValue (open val entity: RdfEntity, private val statement: Statement) : IDataPropertyValue {

//    protected val nsRdf=RdfNamespaces.getByPrefix("rdf")
    protected fun getObject(): RDFNode {
        return statement.`object`
    }

    fun getNamespaceUri():String{
       return statement.predicate.nameSpace
    }

    override fun getName():String{
        return statement.predicate.localName
    }

    override fun getNamespace(): Namespace {
       return Namespace(
            entity.model.coreObject.getNsURIPrefix(getNamespaceUri()),
            getNamespaceUri()
       )
    }

    override fun getProperty(): DataProperty {
        return DataProperty(getNamespace(),getName(),isReference())
    }

}