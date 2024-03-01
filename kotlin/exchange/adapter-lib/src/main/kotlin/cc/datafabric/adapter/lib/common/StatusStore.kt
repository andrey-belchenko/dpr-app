package cc.datafabric.adapter.lib.common

import cc.datafabric.adapter.lib.common.TimeUtils.toFormattedString
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.platform.client.PlatformClientTasks
import java.time.ZonedDateTime


object StatusStore {


    private val collection by lazy {
        val db= MongoDbClient.instance.getDatabase(Config.get("adpMongoServDb"))
        val col = db.getCollection("statusStore")
        col.createIndex(
            Indexes.ascending("adpName", "varName"),
            IndexOptions().unique(true)
        )
        col
    }

    fun get(name:String):Any? {
        return Logger.traceFun (name) {
            val params = Document()
            params["adpName"] = Config.get("adpName")
            params["varName"] = name
            val result = collection.find(params).firstOrNull()
            Logger.traceObject(result)
            if (result==null){
                return@traceFun null
            }
            return@traceFun result["value"]
        }
    }

    fun set(name:String, value:Any){
        Logger.traceFun (name,value) {
            val doc = Document()
            doc["varName"] = name
            doc["adpName"] = Config.get("adpName")
            remove(doc)
            doc["value"] = value
            Logger.traceObject(doc)
            collection.insertOne(doc)
        }
    }

    fun set(name:String,value: ZonedDateTime) {
        set(name,value.toFormattedString())
    }

    fun getInt(name:String):Int? {
        val value = get(name) ?: return  null
        return value as Int?
    }

    fun getZonedDateTime(name:String): ZonedDateTime?{
        val value = get(name) ?: return  null
        return TimeUtils.parseZonedDateTimeString(value.toString())
    }

    private fun remove(params:Document){
        Logger.traceFun {
            Logger.traceObject(params)
            collection.deleteOne(params)
        }
    }



}