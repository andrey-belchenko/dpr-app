package cc.datafabric.adapter.lib.data


interface IDataEntity {
    fun getProperties(): Map<String, MutableList<IDataPropertyValue>>
    fun addPropertyValue(property: DataProperty, value: String)
    fun getClass(): DataClass?
    fun getId(): String
    fun getUri(): String
    fun isChanged():Boolean
}