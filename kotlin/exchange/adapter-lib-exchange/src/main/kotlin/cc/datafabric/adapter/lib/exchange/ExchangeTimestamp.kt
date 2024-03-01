package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.common.MongoDbClient
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.BsonDateTime
import org.bson.Document
import java.util.*

object ExchangeTimestamp {

    private val db by lazy {
        MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))

    }


    fun now(database: MongoDatabase): BsonDateTime {
        //todo оптимизировать c учетом мест где это используется
        return Logger.traceFun {
            ExchangeDummy.initialize()
            val col = database.getCollection(Names.Collections.dummy)
//            if (!col.find().any()){
//                col.insertOne(Document())
//            }
            val result =  col.aggregate(
                listOf(
                    Document("\$project",
                        Document(
                            mapOf(
                                "_id" to false,
                                "value" to "\$\$NOW"
                            )
                        )
                    )
                )
            )
            val value = result.first()?.get("value") as Date
            return@traceFun BsonUtils.getTimeStampFromValue(value)!!
        }

    }

    fun now(): BsonDateTime {
       return now(db)
    }
}