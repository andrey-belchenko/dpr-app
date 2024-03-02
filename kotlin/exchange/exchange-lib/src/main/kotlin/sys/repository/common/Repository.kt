package cc.datafabric.linesapp.sys.repository.common

import cc.datafabric.linesapp.sys.repository.common.dto.*
import cc.datafabric.linesapp.sys.repository.common.query.Query

interface Repository {
    fun findEntities(filter: RepositoryEntityFilter): Iterable<RepositoryEntity>
    fun findLinks(filter: RepositoryLinkFilter): Iterable<RepositoryLink>
    fun save(diff: RepositoryDiff)
    fun executeQueries(queries: Iterable<Query>): RepositoryDataSet {
        val dataSet = RepositoryDataSet()
        queries.forEach {query->
            executeQuery(dataSet, query )
        }
        return dataSet
    }

    private fun executeQuery(dataSet: RepositoryDataSet, query: Query) {
        val entities = mutableListOf<RepositoryEntity>()
        val links = mutableListOf<RepositoryLink>()
        next(query, null, entities, links)
        dataSet.addEntities(entities.distinctBy { it.id })
        val ids = dataSet.entities.map { it.id }.toSet()
        dataSet.addLinks(links
            .distinctBy { Triple(it.fromId, it.predicate, it.toId) }
            .filter {
                ids.contains(it.fromId) && ids.contains(it.toId)
            }
        )
    }


    private fun next(
        query: Query,
        entityFilter: RepositoryEntityFilter?,
        allEntities: MutableList<RepositoryEntity>,
        allLinks: MutableList<RepositoryLink>,
    ) {

        var actualEntityFilter = entityFilter
        if (actualEntityFilter == null) {
            actualEntityFilter = RepositoryEntityFilter()
            actualEntityFilter.id = query.idFilterValue
            actualEntityFilter.type = query.typeFilterValue
            actualEntityFilter.code = query.codeFilterValue
        }

        val entities = findEntities(actualEntityFilter)
        entities.forEach {
            it.baseCode = it.code
        }
        allEntities.addAll(entities)
        if (entities.any()) {
            val ids = entities.map { it.id }.toSet()
            query.linkQueries.forEach { linkQuery ->
                val linkFilter = RepositoryLinkFilter()
                linkFilter.fromId = ids
                linkFilter.predicate = linkQuery.property
                val links = findLinks(linkFilter)
                allLinks.addAll(links)
            }
            query.subQueries.forEach { subQuery ->
                val linkFilter = RepositoryLinkFilter()
                linkFilter.fromId = ids
                linkFilter.predicate = subQuery.property
                linkFilter.toType = subQuery.query.typeFilterValue
                val links = findLinks(linkFilter)
                allLinks.addAll(links)
                if (links.any()) {
                    val entityIds = links.map { it.toId }
                    val nextEntityFilter = RepositoryEntityFilter()
                    nextEntityFilter.id = entityIds
                    next(subQuery.query, nextEntityFilter, allEntities, allLinks)
                }
            }
        }
    }
}


