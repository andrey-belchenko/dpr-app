package cc.datafabric.linesapp.sys.repository.common.dto



import com.google.gson.Gson

class RepositoryDataSet {
    // для сериализация нужен тип List с MutableList не работает
    var links: List<RepositoryLink> = listOf()
    var entities: List<RepositoryEntity> = listOf()

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun formJson(value: String): RepositoryDataSet {
            val gson = Gson()
            return gson.fromJson(value, RepositoryDataSet::class.java)
        }
    }

    fun addEntities(items:Iterable<RepositoryEntity>){
        val newItems = entities.toMutableList()
        newItems.addAll(items)
        entities = newItems.toList()
    }

    fun addLinks(items:Iterable<RepositoryLink>){
        val newItems = links.toMutableList()
        newItems.addAll(items)
        links = newItems.toList()
    }




}