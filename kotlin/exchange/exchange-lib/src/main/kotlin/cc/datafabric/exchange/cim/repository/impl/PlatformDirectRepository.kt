package cc.datafabric.exchange.cim.repository.impl

import cc.datafabric.exchange.cim.repository.common.Repository
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryEntity
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryEntityFilter
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryLink
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryLinkFilter
import cc.datafabric.exchange.lib.sys.Logger
import cc.datafabric.linesapp.sys.repository.common.dto.RepositoryDiff
import java.sql.Connection
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.google.gson.Gson
import com.google.gson.JsonObject




class PlatformDirectRepository : Repository {

    // todo защититься от возможности sql инъекций
    companion object {
        private val connectionString = "jdbc:postgresql://crdbs.oastu.lan:26257/yantar2?sslmode=disable&user=roach&password=chars"
        private val config = HikariConfig().apply {
            jdbcUrl = connectionString
            maximumPoolSize = 20
        }

        private val dataSource = HikariDataSource(config)

        fun getConnection(): Connection = dataSource.connection

        fun close() = dataSource.close()
    }

    private  fun addFilter(whereClause:StringBuilder,field:String,values: Iterable<String>?) {
        if (values?.any() == true) {
            val value = values!!.joinToString(",") { "'$it'" }
            whereClause.appendLine("""and "$field" in ($value)""")
        }
    }

    override fun findEntities(filter: RepositoryEntityFilter): Iterable<RepositoryEntity> {
        Logger.traceFunBeg()
        val result = mutableMapOf<String, RepositoryEntity>()
        val connection: Connection = getConnection()
        val statement = connection.createStatement()
        val whereClause =  StringBuilder()
        addFilter(whereClause,"iri",filter.id)
        addFilter(whereClause,"type",filter.type)
        val query = """
            select * from 
                "Entities" e 
            where 
                current_timestamp between "actualFrom" and "actualTo" 
                $whereClause
        """.trimIndent()
        Logger.traceData(query)
        val resultSet = statement.executeQuery(query)
        while (resultSet.next()) {
            val repositoryEntity = RepositoryEntity(resultSet.getString("iri"),resultSet.getString("type"))
            val modelText = resultSet.getString("model")
            val gson = Gson()
            val modelData: JsonObject = gson.fromJson(modelText, JsonObject::class.java)
            modelData.entrySet().forEach { (key, value) ->
                repositoryEntity.attributes[key] =  value
            }
            result[repositoryEntity.id] =  repositoryEntity
        }
        statement.close()
        connection.close()
        Logger.traceFunEnd()
        return result.values
    }


    override fun findLinks(filter: RepositoryLinkFilter): Iterable<RepositoryLink> {
        Logger.traceFunBeg()
        val result = mutableListOf<RepositoryLink>()
        val connection: Connection = getConnection()
        val statement = connection.createStatement()
        val whereClause =  StringBuilder()
        addFilter(whereClause,"fromIri",filter.fromId)
        addFilter(whereClause,"toIri",filter.toId)
        addFilter(whereClause,"fromType",filter.fromType)
        addFilter(whereClause,"toType",filter.toType)
        addFilter(whereClause,"predicate",filter.predicate)
        val query = """
            select * from
                "Links" e
            where
                current_timestamp between "actualFrom" and "actualTo"
                $whereClause
        """.trimIndent()
        Logger.traceData(query)
        val resultSet = statement.executeQuery(query)
        while (resultSet.next()) {
            val item = RepositoryLink(
                fromId = resultSet.getString("fromIri"),
                fromType = resultSet.getString("fromType"),
                predicate = resultSet.getString("predicate"),
                toId = resultSet.getString("toIri"),
                toType = resultSet.getString("toType"),
            )
            result.add(item)
        }
        statement.close()
        connection.close()
        Logger.traceFunEnd()
        return result.distinctBy { Triple(it.fromId, it.toId, it.predicate) }
    }

    override fun save(diff: RepositoryDiff) {
        TODO("Not yet implemented")
    }




}