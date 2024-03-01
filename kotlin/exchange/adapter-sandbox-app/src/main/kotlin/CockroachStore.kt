package cc.datafabric.adapter.sandbox.app

import cc.datafabric.adapter.lib.sys.Logger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object CockroachStore {

    private fun getConnection(): Connection {
//        val value = DriverManager.getConnection("jdbc:postgresql://195.19.96.240:26257/mrsk", "root", null)
//        val value = DriverManager.getConnection("jdbc:postgresql://cockroach.astu.lan:26257/siber", "root", null)
        val value = DriverManager.getConnection("jdbc:postgresql://cockroach.astu.lan:26257/mrsk_sk11", "root", null)
        value.autoCommit = true
        return value
    }

    //jdbc:postgresql://localhost:26257/yantar
    private fun execute(query: String) {
        getConnection().use { connection ->
            connection.prepareStatement(query).use {
                it.execute()
            }
        }
    }

    fun initTables() {
        initEntitiesStagingTable()
        initLinksStagingTable()
    }

    private fun initEntitiesStagingTable() {
        execute("drop table if exists stg_entities")
        execute(
            """
            CREATE TABLE IF NOT EXISTS stg_entities (
               entity_id VARCHAR(1024) NOT NULL, 
               "type" VARCHAR(1024) NOT NULL, 
               model jsonb NULL
            )
        """.trimIndent()
        )
    }

    private fun initLinksStagingTable() {
        execute("drop table if exists stg_links")
        execute(
            """
            CREATE TABLE IF NOT EXISTS stg_links (
               from_entity_id VARCHAR(1024) NOT NULL, 
               to_entity_id VARCHAR(1024) NOT NULL, 
               name VARCHAR(1024) NOT NULL
            )
        """.trimIndent()
        )
    }

    fun initProfileStagingTable() {
        Logger.traceFun {
            execute("drop table if exists stg_profile")
            execute(
                """
            CREATE TABLE IF NOT EXISTS stg_profile (
               "type" VARCHAR(1024) NOT NULL, 
               domain VARCHAR(1024) NOT NULL, 
               predicate VARCHAR(1024) NOT NULL,
               range VARCHAR(1024) NOT NULL
               
            )
        """.trimIndent()
            )
        }
    }

    fun importEntities() {
        execute(
            """
            IMPORT INTO stg_entities (
                entity_id,
                "type",
                model
              ) 
              CSV DATA (
                'userfile://defaultdb.public.userfiles_root/import/entities.csv'
                );
        """.trimIndent()
        )
    }

    fun importLinks() {
        execute(
            """
            IMPORT INTO stg_links (
                from_entity_id,
                name,
                to_entity_id
              ) 
              CSV DATA (
                'userfile://defaultdb.public.userfiles_root/import/links.csv'
                );
        """.trimIndent()
        )
    }


    fun importProlile() {
        Logger.traceFun {
            execute(
                """
            IMPORT INTO stg_profile (
                 "type"  , 
               domain  , 
               predicate ,
               range 
              ) 
              CSV DATA (
                'userfile://defaultdb.public.userfiles_root/import/profile.csv'
                );
        """.trimIndent()
            )
        }

    }


}