package cc.datafabric.adapter.lib.data


interface IDataPropertyValueResource:IDataPropertyValue {
    fun getValueClass(): DataClass?
}