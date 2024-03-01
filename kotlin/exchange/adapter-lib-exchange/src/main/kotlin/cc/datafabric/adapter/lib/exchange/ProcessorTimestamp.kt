package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.common.MongoDbClient
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.exchange.common.LoggerExtension.traceObject
import org.bson.BsonDateTime
import cc.datafabric.adapter.lib.sys.ConfigNames

object ProcessorTimestamp {

    private val processorName = Config.get(ConfigNames.processorName)

    private val db by lazy {
        MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))

    }

    private val procTimestampCol by lazy {
        val col = db.getCollection(Names.Collections.processorTimestamp)

        col
    }




    fun set(value: BsonDateTime) {
        Logger.traceFun(value) {

            procTimestampCol.deleteMany(Document(Names.Fields.processorName,processorName))
            val doc = Document()
            doc["value"] = value
            doc[Names.Fields.processorName] = processorName
            procTimestampCol.insertOne(doc)
        }
    }

    fun get():BsonDateTime {
        return Logger.traceFun () {
            val doc = procTimestampCol.find(Document(Names.Fields.processorName,processorName)).first()
            var value = BsonUtils.getTimeStampFromValue(doc?.getDate("value"))
            if (value==null) {
                value = BsonUtils.getMinTimeStamp()
            }
            Logger.traceObject(value)
            return@traceFun value
        }
    }

    private val procColTimestampCol by lazy {
        val col = db.getCollection(Names.Collections.processorCollectionTimestamp)
        col
    }


    fun set(collectionName:String,value: BsonDateTime) {
        Logger.traceFun(collectionName,value) {
            val doc = Document()
            doc[Names.Fields.processorName] = processorName
            doc[Names.Fields.collectionName] = collectionName
            procColTimestampCol.deleteMany(doc)
            doc["value"] = value
            procColTimestampCol.insertOne(doc)
        }
    }

    fun get(collectionName:String):BsonDateTime {
        val filter = Document()
        filter[Names.Fields.processorName] = processorName
        filter[Names.Fields.collectionName] = collectionName
        val doc = procColTimestampCol.find(filter).first()
        var value = BsonUtils.getTimeStampFromValue(doc?.getDate("value"))
        if (value==null) {
            value = BsonUtils.getMinTimeStamp()
        }
        return  value
    }


}