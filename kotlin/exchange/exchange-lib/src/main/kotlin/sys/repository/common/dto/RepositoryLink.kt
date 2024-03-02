package cc.datafabric.linesapp.sys.repository.common.dto


class RepositoryLink(
    val fromId: String,
    val toId: String,
    val fromType: String,
    val toType: String,
    val predicate: String,

){
    fun getKey():Triple<String,String,String>{
        return Triple(fromId,predicate,toId)
    }

    fun copy(): RepositoryLink {
        return RepositoryLink(fromId,toId,fromType,toType,predicate)
    }
}