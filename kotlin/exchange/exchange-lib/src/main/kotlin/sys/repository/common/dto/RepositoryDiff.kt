package cc.datafabric.linesapp.sys.repository.common.dto

import com.google.gson.Gson

class RepositoryDiff {
    var createdEntities: List<RepositoryEntity> = listOf()
    var changedEntities: List<RepositoryEntity> = listOf()
    var deletedEntities: List<RepositoryEntity> = listOf()
    var createdLinks: List<RepositoryLink> = listOf()
    var deletedLinks: List<RepositoryLink> = listOf()

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    fun changesInfo(): String {
        return "createdEntities: ${createdEntities.count()}, " +
                "changedEntities: ${changedEntities.count()}, " +
                "deletedEntities: ${deletedEntities.count()}, " +
                "createdLinks: ${createdLinks.count()}, " +
                "deletedLinks: ${deletedLinks.count()}"
    }
    companion object{
        fun compareDataSets(initial: RepositoryDataSet, actual: RepositoryDataSet): RepositoryDiff {
            val diffSet = RepositoryDiff()
            val initialEntitiesById =  initial.entities.associateBy { it.id }
            val createdEntities =  mutableListOf<RepositoryEntity>()
            val changedEntities =  mutableListOf<RepositoryEntity>()
            actual.entities.forEach { actualEnt ->
                val fromEnt = initialEntitiesById[actualEnt.id]
                if (fromEnt == null) {
                    createdEntities.add( actualEnt.copyWithAttributes())
                } else {
                    var changedEnt: RepositoryEntity? = null
                    actualEnt.attributes.forEach { attr ->
                        if (attr.value != fromEnt.attributes[attr.key]) {
                            if (changedEnt == null) {
                                changedEnt = actualEnt.copyWithoutAttributes()
                            }
                            changedEnt!!.attributes[attr.key] = attr.value
                        }
                    }
                    if (changedEnt != null) {
                        changedEnt!!.extraAttributes = actualEnt.extraAttributes
                        changedEntities.add(changedEnt!!)
                    }
                }

            }


            val actualEntKeys =  actual.entities.map { it.id }.toSet()
            val deletedEntities =  mutableListOf<RepositoryEntity>()
            initial.entities.forEach{ initialEnt ->
                if (!actualEntKeys.contains(initialEnt.id)){
                    deletedEntities.add(initialEnt.copyWithAttributes())
                }
            }
            val initialLinksKeys =  initial.links.map { it.getKey() }.toSet()
            val createdLinks  =  mutableListOf<RepositoryLink>()
            actual.links.forEach{ actualLink ->
                if (!initialLinksKeys.contains(actualLink.getKey())){
                    createdLinks.add(actualLink.copy())
                }
            }
            val actualLinksKeys =  actual.links.map { it.getKey() }.toSet()
            val deletedLinks  =  mutableListOf<RepositoryLink>()
            initial.links.forEach{ initialLink ->
                if (!actualLinksKeys.contains(initialLink.getKey())){
                    deletedLinks.add(initialLink.copy())
                }
            }
            diffSet.createdEntities = createdEntities
            diffSet.changedEntities = changedEntities
            diffSet.deletedEntities = deletedEntities
            diffSet.createdLinks = createdLinks
            diffSet.deletedLinks = deletedLinks

            return diffSet
        }
    }
}