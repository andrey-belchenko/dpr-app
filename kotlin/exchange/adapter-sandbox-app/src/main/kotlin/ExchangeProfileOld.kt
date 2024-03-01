import cc.datafabric.adapter.lib.common.MongoDbClient
import cc.datafabric.adapter.lib.data.ConstNamespaces
import cc.datafabric.adapter.lib.data.Namespace

import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document

object ExchangeProfileOld {

    class ClassInfo (val name:String){
        val properties:MutableMap<String,PropertyInfo> = mutableMapOf()
//        val singleLinkProperties:MutableMap<String,PropertyInfo> = mutableMapOf()
    }

    class PropertyInfo (
        val className: String,
        val name:String,
        val id:String,
        val typeName:String,
        val isMultiple:Boolean,
        val isLiteral:Boolean,
        var inverseOf:String? =  null
    )

    val profileMap = mutableMapOf<String,ClassInfo>()
    val properties:MutableList<PropertyInfo> = mutableListOf()
    private val db by lazy {
//        val obj = MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))
        val obj = MongoDbClient.instance.getDatabase("test_profile")
        obj
    }
    

    private fun initialize() {
        val profileClassesDocs = db.getCollection("sys_ProfileClasses").find()
        profileClassesDocs.forEach { cls->
            val className= cls["name"].toString()
            val props = cls["properties"] as MutableList<Document>
            //todo добавить в профиль
            // или хотя бы пока сделать декомпозицию тут
            if (className=="AccountPartLine") {
              var extraProp = Document.parse(  """
                {
                    "class" : "AccountPartLine",
                    "prop" : "AccountPartLine.Towers",
                    "propType" : "Tower",
                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
                    "multiplicity" : "0..n",
                    "isLiteral" : false
                }
                """
              )
              props.add(extraProp)


            }

            if (className=="Line") {
                var extraProp = Document.parse(  """
                {
                    "class" : "Line",
                    "prop" : "Line.Disconnectors",
                    "propType" : "Disconnector",
                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
                    "multiplicity" : "0..n",
                    "isLiteral" : false
                }
                """
                )
                props.add(extraProp)


            }

            if (className=="Disconnector") {
                var extraProp = Document.parse(  """
                {
                    "class" : "Disconnector",
                    "prop" : "Disconnector.Tower",
                    "propType" : "Tower",
                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
                    "multiplicity" : "0..1",
                    "isLiteral" : false
                }
                """
                )
                props.add(extraProp)
                extraProp = Document.parse(  """
                {
                    "class" : "Disconnector",
                    "prop" : "Disconnector.Line",
                    "propType" : "Line",
                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
                    "multiplicity" : "0..1",
                    "isLiteral" : false
                }
                """
                )
                props.add(extraProp)
            }
            if (className=="Tower") {
                var extraProp = Document.parse(  """
                {
                    "class" : "Tower",
                    "prop" : "Tower.AccountPartLine",
                    "propType" : "AccountPartLine",
                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
                    "multiplicity" : "0..1",
                    "isLiteral" : false
                }
                """
                )
                props.add(extraProp)

                extraProp = Document.parse(  """
                {
                    "class" : "Disconnector",
                    "prop" : "Tower.Disconnectors",
                    "propType" : "Disconnector",
                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
                    "multiplicity" : "0..n",
                    "isLiteral" : false
                }
                """
                )
                props.add(extraProp)

            }
            if (profileMap[className]==null){
                profileMap[className] = ClassInfo(className)
            }
            props.forEach {
                var isMultiple = listOf("0..n", "1..n").contains(it["multiplicity"].toString())

                //todo есть такие свойства, одновременно литерал и ссылка
                // при этом multiplicity "0..n"
                // обратная связь не находится
                // ставлю им isMultiple = false
                // сейчас предполагается что не должно быть: связей 0..n без обратной связи, литерал массивов, связей многие ко многим
                // если такие случаи бывают требуется анализ и пересмотр решения

//                {
//                    "class" : "PowerTransformerEnd",
//                    "prop" : "PowerTransformerEnd.ratedU",
//                    "propType" : "Voltage",
//                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
//                    "multiplicity" : "0..n",
//                    "isLiteral" : true,
//                    "_id" : ObjectId("63c758b3f172bd34f670e2da")
//                }
                if (it["isLiteral"] as Boolean){
                    isMultiple = false
                }
                //todo костыль, исправить в профиле и убрать
                if (listOf("Tower.StartTower","Tower.EndTower").contains(it["prop"].toString())){
                    isMultiple = true
                }
                val propInfo = PropertyInfo(
                    className = className,
                    name = it["prop"].toString(),
                    id = it["prop"].toString().replace(".", "_"),
                    isMultiple = isMultiple,
                    typeName = it["propType"].toString(),
                    isLiteral =  it["isLiteral"] as Boolean
                )
                profileMap[className]!!.properties[propInfo.id] = propInfo
//                if (!propInfo.isMultiple && !propInfo.isLiteral) {
//                    profileMap[className]!!.singleLinkProperties[propInfo.id] = propInfo
//                }
                properties.add(propInfo)
            }
        }
        initInverse()
    }

    private fun initInverse(){

        properties.forEach { prp->

            //todo добавить в профиль
            // или хотя бы пока сделать декомпозицию тут
            if (prp.id=="IdentifiedObject_childObjects"){
                prp.inverseOf="IdentifiedObject_ParentObject"
            } else if (prp.id=="IdentifiedObject_ParentObject") {
                prp.inverseOf="IdentifiedObject_childObjects"
            }else if (prp.id=="AccountPartLine_Towers"){
                prp.inverseOf="Tower_AccountPartLine"
            } else if (prp.id=="Tower_AccountPartLine") {
                prp.inverseOf="AccountPartLine_Towers"
            }else if (prp.id=="Line_Disconnectors"){
                prp.inverseOf="Disconnector_Line"
            } else if (prp.id=="Disconnector_Line") {
                prp.inverseOf="Line_Disconnectors"
            }else if (prp.id=="Tower_Disconnectors"){
                prp.inverseOf="Disconnector_Tower"
            } else if (prp.id=="Disconnector_Tower") {
                prp.inverseOf="Tower_Disconnectors"
            }

            else if (prp.id=="EquipmentContainer_Equipments"){
                prp.inverseOf="Equipment_EquipmentContainer"
            } else if (prp.id=="Equipment_EquipmentContainer") {
                prp.inverseOf="EquipmentContainer_Equipments"
            }
            else{
                var inverse = properties.filter {
                    it.className==prp.typeName && it.typeName==prp.className
                }



                if (inverse.count()>1){
                    inverse = inverse.filter { it.id.split('_').last() == prp.id.split('_').last() }
                }

                if (inverse.count()==1){
                    prp.inverseOf=inverse.first().id
                }
            }

        }

    }
    init {
        initialize()
    }


    fun ensureExists (className: String,propertyName: String){
        if (profileMap[className]==null){
            throw Exception("Class $className does not exist")
        }
        if (profileMap[className]!!.properties[propertyName]==null){
            throw Exception("Property $className.$propertyName does not exist")
        }
    }

    fun getInverseName(className: String,propertyName: String):String?{
        ensureExists(className,propertyName)
        return profileMap[className]!!.properties[propertyName]!!.inverseOf
    }
    fun isMultiple(className: String,propertyName: String):Boolean{
        ensureExists(className,propertyName)
        return profileMap[className]!!.properties[propertyName]!!.isMultiple
    }

    fun isLiteral(className: String,propertyName: String):Boolean {
        ensureExists(className, propertyName)
        return profileMap[className]!!.properties[propertyName]!!.isLiteral
    }

    //todo костыль
    fun getPropertyNamespace(propertyName: String, isSk:Boolean):Namespace {

        if (!isSk){
            return  ConstNamespaces.cim
        }
        if (propertyName == "IdentifiedObject_ParentObject") {
            return ConstNamespaces.me
        } else if (propertyName == "IdentifiedObject_childObjects"){
            return ConstNamespaces.me
        }
        return ConstNamespaces.cim
    }
//    fun getClass(className: String):ClassInfo {
//        return  profileMap[className]!!
//    }
//    fun hasClass(className: String):Boolean{
//        return profileMap.containsKey(className)
//    }
//    fun hasProperty(className: String,propertyName: String):Boolean{
//        return getProperty(className,propertyName)!=null
//    }
//    private fun getProperty(className: String, propertyName: String):Document?{
//        return profileMap[className]!![propertyName]
//    }
//
//    fun getPropertyType(className: String,propertyName: String):String{
//        return profileMap[className]!![propertyName]!!["propType"]!!.toString()
//    }
}