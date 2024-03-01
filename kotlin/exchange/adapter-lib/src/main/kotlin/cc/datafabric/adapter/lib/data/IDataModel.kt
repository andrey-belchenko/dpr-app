package cc.datafabric.adapter.lib.data



interface IDataModel {
    fun getEntities(): Iterable<IDataEntity>
    fun getClass(classIri: String): DataClass?

    fun createEntity(uri: String, dataClass: DataClass?): IDataEntity

    fun getNamespaces():Iterable<Namespace>
    fun setNamespace(namespace:Namespace)

//    var diff: DataDiff?
}