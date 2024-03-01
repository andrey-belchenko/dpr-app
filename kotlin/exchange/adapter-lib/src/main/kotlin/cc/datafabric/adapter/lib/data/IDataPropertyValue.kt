package cc.datafabric.adapter.lib.data


interface IDataPropertyValue {
    fun getName(): String
    fun getNamespace(): Namespace
    fun getProperty(): DataProperty
    fun getValueText(): String
    fun isReference(): Boolean
}