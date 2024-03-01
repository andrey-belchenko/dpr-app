package cc.datafabric.adapter.lib.data


class DataClass {

    var namespace: Namespace
    var name:String

    constructor(namespace: Namespace, name:String) {
        this.name = name
        this.namespace = namespace
        check()
    }
    constructor(uri:String) {
        val uriParts = uri.split("#")
        val namespaceUri = uriParts[0]+"#"
        this.name = uriParts[1]
        this.namespace= Namespace(namespaceUri)
        check()
    }

    private  fun check(){
        if (name == ""){
            throw Exception("Type name could not be empty string")
        }
    }

    fun getUri():String {
        return namespace.uri + name
    }
}
