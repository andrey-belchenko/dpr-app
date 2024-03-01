package cc.datafabric.adapter.lib.rdf


import cc.datafabric.adapter.lib.data.*
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement

class RdfEntity internal constructor(val model: RdfModel, private val resource: Resource) : IDataEntity {

//    private val nsRdf=RdfNamespaces.getByPrefix("rdf")
    private val properties:MutableMap<String, MutableList<IDataPropertyValue>> = mutableMapOf()

    private var dataClass: DataClass? = null
    private var dataClassByClassName: DataClass? = null
    override fun getProperties(): Map<String, MutableList<IDataPropertyValue>> {
        return properties!!
    }



    private fun init() {
        //Проверяется только localName. Вероятность коллизий? Доделать проверку с учетом namespace при необходимости.
        resource.listProperties().forEach {
           initProperty(it)
        }
    }

    private fun initProperty(statement: Statement){
        //todo пока убрал, нужно разобраться зачем это условие
        // mRID используется на проекте МРСК для получения отладочных пометок из СК на заведенных связях
        val skipProperties:List<String> = listOf(
//            "IdentifiedObject.mRID",
//            "IdentifiedObject.SKID"
        )
        val key = statement!!.predicate.localName
        if (skipProperties.contains(key)) return
        val obj = statement.`object`
        val prop = when {
            obj.isLiteral -> {
                RdfPropertyValueLiteral(this, statement)
            }
            obj.isResource -> {
                RdfPropertyValueResource(this, statement)
            }
            else -> throw NotImplementedError()
        }
        if (key == "type") {
            val type = prop as RdfPropertyValueResource
            dataClass =model.getOrAddClass(type.getValueNamespace(),type.getValueText())
        }
        else if (key == "className"){
            val className = (prop as RdfPropertyValueLiteral).getValueText()
            dataClassByClassName = model.getOrAddClass(ConstNamespaces.cim,className)
        } else
        {
            if (!properties.containsKey(key)){
                properties[key] =  mutableListOf()
            }
            properties[key]!!.add(prop)
        }


    }

    override fun  addPropertyValue(property: DataProperty, value: String) {
        val mod = model.coreObject
        val prop = mod.createProperty(property.namespace.uri, property.name)
        val st =
            if (property.isReference!!) {
                mod.createStatement(
                    resource,
                    prop,
                    mod.createResource(property.namespace.uri + value)
                )
            } else {
                mod.createStatement(
                    resource,
                    prop,
                    mod.createLiteral(value)
                )
            }
        mod.add(st)
        initProperty(st)
    }



    override fun getId():String{

//        return resource.localName обрезает цифры в начале
        return  resource.uri.split("#").last()
    }

//    private var newId:String?=null
//    fun setId(value:String) {
//        newId = value
//    }

    override fun getUri():String {
        return resource.uri
    }

//    fun getId():String {
//        if (newId != null) {
//            return newId!!
//        }
//        return getOriginalId()
//    }

    override fun isChanged():Boolean {
        //вроде как можно ориентироваться по осутствию свойства type в модели
        // чтобы понять что объект меняется а не создается
        return dataClass == null
    }

    override fun getClass(): DataClass? {
        //todo У измененных объектов не заполнен type, но есть свойство className
        // ориентироваться по нему не совсем корректно, т.к. в нем не указывается namespace класса
        // сейчас в этом случае ставится namespace cim.
        // Еще раз пересмотреть эту логику, возможно есть более правильное решение
        // класс нужен чтобы отфильтровать только нужные объекты из диффа
        // возможно решением будет фильтрация на уровне API
        return dataClass ?: dataClassByClassName
    }
    init {
        init()
    }

}