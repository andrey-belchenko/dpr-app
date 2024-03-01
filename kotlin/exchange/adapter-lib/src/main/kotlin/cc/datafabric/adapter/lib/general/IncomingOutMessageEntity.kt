package cc.datafabric.adapter.lib.general

import cc.datafabric.adapter.lib.common.BsonUtils
import org.bson.Document
/**
 * Entity (JSON object, document) in the message payload
 */

class IncomingOutMessageEntity (val message: IncomingOutMessage, val obj: Document) {
    // todo не проверено после изменений , проверить и почистить закомменированный код
    private var sourceKey:String? = null
    init {
        if (message.isWithSourceKeys){
            sourceKey=obj["@id"].toString().split(":").last()
            // todo убрать, костыль для демонстрации по сибири
            if (sourceKey=="110"){
                sourceKey = "110 КВ"
            }
        }
    }

    private var namespacePrefix:String?=null
    private var typeName:String?=null
//    private var namespaceIri :String?=null
//    private var namespace:Namespace? = null
    private var typeIri :String?=null
    private fun prepareTypeInfo() {
        if (typeName==null){
            val type = obj["@type"].toString() // пример "@type": "astu:Substation"
            val typeParts = type.split(":")

            if (typeParts.count()!=2){
                throw  Exception ("'@type' field does not exist or has incorrect value \n ${BsonUtils.toJsonString(obj)}")
            }
            namespacePrefix = typeParts[0] // пример "astu"
            typeName = typeParts[1] // пример "Substation"
            val namespaceIri = message.getNamespaceUri(namespacePrefix!!)  // пример "astu" -> "http://ontology.adms.ru/UIP/md/2021-1#"
            typeIri = namespaceIri + typeName //пример http://ontology.adms.ru/UIP/md/2021-1#Substation
        }
    }

    fun getTypeName():String {
        prepareTypeInfo()
        return typeName!!
    }

    fun getTypeIri():String {
        prepareTypeInfo()
        return typeIri!!
    }

    fun getNamespacePrefix():String {
        prepareTypeInfo()
        return namespacePrefix!!
    }

//    fun getPref():String {
//        prepareTypeInfo()
//        return prefix!!
//    }
    private var platformKey :String?=null
    fun setPlatformKey(value:String) {
        platformKey = value
        obj["@id"] =
            getNamespacePrefix() + ":" + platformKey // пример astu:Substation_8a84a5b9-09ba-43a9-85d2-4149f40773c9
    }

//    private fun getMrid():String? {
//        return mrid
//    }

    fun getPlatformKey():String? {
        if (platformKey==null) {
            return null
        }
        return   platformKey  // Substation_8a84a5b9-09ba-43a9-85d2-4149f40773c9
    }

    fun getSourceKey():String? {
        return sourceKey
    }

    fun getSourceSystem():String {
        return message.getSource()
    }

    fun getFieldValue(name:String):Any?{
        return obj[name]
    }

    fun setFieldValue(name:String, value: Any?){
         obj[name] = value
    }


}