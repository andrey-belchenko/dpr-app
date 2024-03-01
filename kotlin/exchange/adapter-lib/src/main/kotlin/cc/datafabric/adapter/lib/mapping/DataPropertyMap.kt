package cc.datafabric.adapter.lib.mapping

import cc.datafabric.adapter.lib.data.DataClass
import cc.datafabric.adapter.lib.data.DataClassProperty


class DataPropertyMap  {
    //todo завести классы под эту структуру
    private val map = mutableMapOf<String,MutableMap<String, MutableList<DataClassProperty>>>()

    //todo изначально был маппинг по classIri, но для измененных объектов не определить Namespace класса
    // проанализировать,может быть можно доделать или упростить
    fun add(from: DataClassProperty, to: DataClassProperty) {
        val className = from.dataClass.name
        if (map[className] == null) {
            map[className] = mutableMapOf()
        }
        if (map[className]!![from.property.getIri()] == null) {
            map[className]!![from.property.getIri()] = mutableListOf()
        }
        map[className]!![from.property.getIri()]?.add(to)
    }

    fun getClassProperty (className:String, propertyIri:String): List<DataClassProperty>?{
        return map[className]?.get(propertyIri)
    }

    fun getClass (className:String): DataClass? {
        return map[className]?.values?.first()?.first()?.dataClass
    }
}