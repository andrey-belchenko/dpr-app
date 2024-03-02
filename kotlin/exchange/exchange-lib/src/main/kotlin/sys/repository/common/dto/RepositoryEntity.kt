package cc.datafabric.linesapp.sys.repository.common.dto


class RepositoryEntity (val id:String, val type:String) {
    var code:String? = null
    var baseCode:String? = null
    val attributes = mutableMapOf<String,Any?>()
    var extraAttributes = mutableMapOf<String,Any?>()

    fun copyWithoutAttributes(): RepositoryEntity {
        val value = RepositoryEntity(id,type)
        value.code =  this.code
        value.baseCode =  this.baseCode
        return value
    }
    fun copyWithAttributes(): RepositoryEntity {
        val value = copyWithoutAttributes()
        attributes.forEach{
            value.attributes[it.key] = it.value
        }
        extraAttributes.forEach{
            value.extraAttributes[it.key] = it.value
        }
        return value
    }
}