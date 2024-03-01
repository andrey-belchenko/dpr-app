package cc.datafabric.adapter.lib.rdf


import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
import cc.datafabric.adapter.lib.data.*
import cc.datafabric.adapter.lib.sys.Logger
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource

class RdfModel internal constructor(internal val coreObject: Model) : IDataModel {

//    override var diff: DataDiff? = null

    private val entities = mutableMapOf<String, RdfEntity>()


    override fun getEntities():Iterable<RdfEntity>{
        return entities.values
    }

//    internal fun getNamespaceByUri(uri:String): Namespace {
//        var value = ConstNamespaces.getByUri(uri)
//        if (value == null) {
//            value = Namespace(coreObject.getNsURIPrefix(uri), uri)
//        }
//        if (diff!=null){
//            val mappedUri= diff!!.getProfileMap().getMappedNamespace(uri)
//            if (mappedUri!=null) {
//                value = mappedUri
//            }
//        }
//        return value
//    }


    private fun listNamespaces() = sequence{
        coreObject.listNameSpaces().toList().forEach {
            yield(Namespace(coreObject.getNsURIPrefix(it),it))
        }
    }

    override fun setNamespace(namespace:Namespace){
        coreObject.setNsPrefix(namespace.prefix,namespace.uri)
    }

    override fun getNamespaces():Iterable<Namespace>{
        return listNamespaces().toList()
    }

    private val classes:MutableMap<String, DataClass> = mutableMapOf()
    internal fun getOrAddClass(classNamespace: Namespace, className:String): DataClass? {
        val classIri = classNamespace.uri + className
        if (classes[classIri] == null) {
            classes[classIri] = DataClass(classNamespace, className)
        }
        return classes[classIri]
    }
    override fun getClass(classIri: String): DataClass?{
        return classes[classIri]
    }
    private fun initEntities(){
        coreObject.listSubjects().forEach {
            createEntity(it!!)
        }
    }

    fun getEntity(id:String): RdfEntity?{
        return entities[id]
    }
    private fun createEntity(resource: Resource): RdfEntity {
        return Logger.traceFun {
            Logger.traceObject(resource)
            val entity = RdfEntity(this, resource)
            entities[entity.getId()] = entity
            return@traceFun entity
        }
    }


    override fun createEntity(uri:String, dataClass: DataClass?): IDataEntity {
        val objRes = coreObject.createResource(uri)
        if (dataClass != null) {
            val typeProp = coreObject.createProperty(ConstNamespaces.rdf.uri, "type")
            val typeRes = coreObject.createResource(dataClass.getUri())
            val st = coreObject.createStatement(objRes, typeProp, typeRes)
            coreObject.add(st)
        }
        return createEntity(objRes)
    }

    init {
        initEntities()
    }

}