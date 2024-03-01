package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.common.MongoDbClient
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import com.mongodb.client.model.UpdateOptions
import org.bson.BsonDateTime
import java.util.Date


object CollectionChangeInfoStore {


    private val collection by lazy {
        val db= MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))

        val col = db.getCollection(Names.Collections.collectionChangeInfo)
        col
    }


    // todo костыль
//    val instance by lazy {
//        val res = MongoClient(MongoClientURI(Config.get("adpTargetMongoUri"))) // todo костыль
//        res
//    }
    // todo костыль
    private val targetDB by lazy {
        ExchangeDatabase.db
//        instance.getDatabase(Config.get("adp_mongo_exchange_db"))
    }
    // todo костыль
    private val targetCollection by lazy {
        collection
//        val db= targetDB
//        val col = db.getCollection("sys_CollectionChangeInfo")
//        col.createIndex(
//            Indexes.ascending("collectionName"),
//            IndexOptions().unique(true)
//        )
//        col.createIndex(
//            Indexes.ascending("changedAt")
//        )
//        col
    }


    val list = mutableListOf<Document>()
    fun commit(){
        Logger.traceFun {
            //todo что то типа транзакции , пока больше похоже на костыль
            // сделано, чтобы сервис отправки в СК-11 видел консистентные изменения

            list.parallelStream().forEach { doc->
                val filter = Document()
                filter[Names.Fields.collectionName] = doc[Names.Fields.collectionName]
                val upd = Document(
                    mapOf(
                        "\$set" to doc,
                        "\$currentDate" to Document(Names.Fields.changedAt,true)
                    )
                )
                targetCollection.updateOne(filter, upd, UpdateOptions().upsert(true))
            }
            list.clear()
        }
    }

    fun set(collectionName:String) {
        Logger.traceFun(collectionName) {
            val doc = Document()
            doc[Names.Fields.collectionName] = collectionName
            list.add(doc)
        }
    }

    // todo удалить вместе со всеми вызовами
    fun setDummy(collectionName:String) {
//        Logger.traceFun(collectionName) {
//            val doc = Document()
//            doc[Names.Fields.collectionName] = collectionName
//            list.add(doc)
//        }
    }
    class CollectionChangeInfo (val name:String,val changedAt: BsonDateTime)
    fun getChangedFrom(changedFrom: BsonDateTime):Iterable<CollectionChangeInfo>{
        return Logger.traceFun (changedFrom) {
            val filter = Document(Names.Fields.changedAt,Document("\$gt",changedFrom))
            return@traceFun collection.find(filter).map {
                CollectionChangeInfo(
                    it[Names.Fields.collectionName].toString(),
                    BsonUtils.getTimeStampFromValue(it[Names.Fields.changedAt] as Date)!!
                )
            }
        }
    }




}