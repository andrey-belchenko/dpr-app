package cc.datafabric.adapter.sandbox.app

import cc.datafabric.adapter.lib.common.BsonUtils
import org.bson.Document

object Profile {
    private val profileClassesDocs = MongoDbStore.find("sys_ProfileClasses")
    val profileMap = mutableMapOf<String,MutableMap<String,Document>>()
    fun initialize() {
        profileClassesDocs.forEach { cls->
            val className= cls["name"].toString()
            if (profileMap[className]==null){
                profileMap[className] = mutableMapOf()
            }
            (cls["properties"] as Iterable<Document>).forEach {
                profileMap[className]!![it["prop"].toString()] = it
            }
        }
    }



    fun hasClass(className: String):Boolean{

        return profileMap.containsKey(className)
    }
    fun hasProperty(className: String,propertyName: String):Boolean{
        return getProperty(className,propertyName)!=null
    }
    private fun getProperty(className: String, propertyName: String):Document?{
        return profileMap[className]!![propertyName]
    }

    fun getPropertyType(className: String,propertyName: String):String{
        return profileMap[className]!![propertyName]!!["propType"]!!.toString()
    }



}