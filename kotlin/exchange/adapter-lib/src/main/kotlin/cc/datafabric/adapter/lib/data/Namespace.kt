package cc.datafabric.adapter.lib.data


class Namespace {

    var prefix:String
    var uri: String

    constructor(prefix:String?, uri: String, isConst:Boolean = false) {
        this.uri = uri
        if (isConst){
            this.prefix = prefix!!
            return
        }
        this.uri = uri
        var pref =
            ConstNamespaces.getByUri(uri)?.prefix ?:
            usedPrefixes[uri] ?:
            prefix
        if (pref==null){
            prefixIndex++
            pref = "ns$prefixIndex"
        }
        usedPrefixes[uri] = pref
        this.prefix = pref

    }


    companion object{
        val usedPrefixes= mutableMapOf<String,String>()
        var prefixIndex = 0
    }
    constructor(uri: String):this(null,uri)
    override fun toString(): String {
        if (prefix==""){
            return """xmlns="$uri""""
        }
        return """xmlns:$prefix="$uri""""
    }
}