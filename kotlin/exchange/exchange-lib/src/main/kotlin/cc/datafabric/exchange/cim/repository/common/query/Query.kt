package cc.datafabric.exchange.cim.repository.common.query

import cc.datafabric.exchange.cim.model.ReflectionUtils
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class Query() {

    var idFilterValue: Set<String>? = null
    var codeFilterValue: Set<String>? = null
    var typeFilterValue: Set<String>? = null

    val subQueries: MutableList<SubQuery> = mutableListOf()
    val linkQueries: MutableList<LinkQuery> = mutableListOf()
    fun filterById(id: String): Query {
        idFilterValue = setOf(id)
        return this
    }

    fun filterByCode(id: String): Query {
        codeFilterValue = setOf(id)
        return this
    }

    fun filterByType(vararg types: KClass<*>): Query {
        typeFilterValue = types.map { ReflectionUtils.getClassSubclasses(it) }.flatten().distinct().toSet()
        return this
    }

    fun include(
        vararg property: KProperty<*>,
        query: ((context: Query) -> Query)? = null
    ): Query {
        val propNames = property.map { ReflectionUtils.getPropFullName(it) }.toSet()
        val subQuery = SubQuery( propNames, Query())
        if (query != null) {
            query(subQuery.query)
        }
        subQueries.add(subQuery)
        return this
    }

    fun includeLinks(
        vararg property: KProperty<*>,
    ): Query {
        val propNames = property.map { ReflectionUtils.getPropFullName(it) }.toSet()
        linkQueries.add(LinkQuery( propNames))
        return this
    }
}