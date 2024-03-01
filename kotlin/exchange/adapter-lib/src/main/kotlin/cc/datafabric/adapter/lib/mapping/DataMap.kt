package cc.datafabric.adapter.lib.mapping

import cc.datafabric.adapter.lib.data.Namespace
import cc.datafabric.adapter.lib.data.DataClass
import cc.datafabric.adapter.lib.data.DataProperty
import cc.datafabric.adapter.lib.common.KeyInfo


class DataMap (
    private val namespaceMap:Map<String, Namespace>?=null,
    private val propertyMap: DataPropertyMap?=null,
    private val keyMap: DataKeyMap?=null
){
    constructor() : this(null)

    fun getMappedNamespace(value:String): Namespace? {
        if (namespaceMap == null) {
            return null
        }
        return namespaceMap[value]
    }

    fun hasPropertyMap():Boolean {
        return propertyMap != null
    }

    fun hasKeyMap():Boolean {
        return keyMap != null
    }

    fun getMappedKey(originalValue:String,isDelete:Boolean): KeyInfo? {
        return keyMap!!.get(originalValue,isDelete)
    }

    fun getMappedKey(originalDataClass: DataClass, newDataClass: DataClass, originalValue:String): KeyInfo? {
        return keyMap!!.get(originalDataClass,newDataClass,originalValue)
    }

    //todo изначально был маппинг по classIri, но для измененных объектов не определить Namespace класса
    // проанализировать,может быть можно доделать или упростить
    fun getMappedProperty(className:String, propertyIri:String): Iterable<DataProperty>? {
        return propertyMap!!.getClassProperty(className, propertyIri)?.map { it.property }
    }
    fun getMappedClass(className:String): DataClass? {
        return propertyMap!!.getClass(className)
    }

    fun commitDeletedKeys(){
        keyMap!!.commitDeleted()
    }
}