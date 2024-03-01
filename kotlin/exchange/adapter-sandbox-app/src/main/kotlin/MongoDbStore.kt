package cc.datafabric.adapter.sandbox.app

import cc.datafabric.adapter.lib.common.MongoDbClient
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*


object MongoDbStore {


    private val db by lazy {
        MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))
    }

    fun dropCollection(collectionName: String) {
        db.getCollection(collectionName).drop()
    }

    private const val insertBatchSize = 1000

    val insertMap = mutableMapOf<String, MutableList<Document>>()

    fun insert(collectionName: String, obj: Document) {
        if (!insertMap.containsKey(collectionName)) {
            insertMap[collectionName] = mutableListOf()
        }
        insertMap[collectionName]!!.add(obj)
        if (insertMap[collectionName]!!.count() == insertBatchSize) {
            applyInsert(collectionName)
        }


    }

   private fun applyInsert(collectionName: String) {
       insertMap[collectionName]?.let { db.getCollection(collectionName).insertMany(it) }
       insertMap[collectionName] = mutableListOf()
    }
    fun applyInsert(){
        insertMap.keys.forEach{
            applyInsert(it)
        }
    }



    fun find(collectionName:String):Iterable<Document> {
       return  db.getCollection(collectionName).find()
    }

}