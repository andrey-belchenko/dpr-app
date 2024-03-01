package cc.datafabric.adapter.lib.data


class DataProperty (val namespace: Namespace, val name:String, val isReference:Boolean?=null)
{
    fun getIri():String {
        return namespace.uri + name
    }
}