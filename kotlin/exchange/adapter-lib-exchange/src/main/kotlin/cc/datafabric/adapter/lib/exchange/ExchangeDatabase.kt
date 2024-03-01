package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.MongoDbClient
import cc.datafabric.adapter.lib.sys.Config
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import com.mongodb.client.gridfs.GridFSBuckets
import org.bson.types.ObjectId
import java.io.InputStream
import java.util.UUID


object ExchangeDatabase {


    private val databasees = mutableMapOf<String, MongoDatabase>()


    fun getCollection(name: String): MongoCollection<Document> {
        var dbSuffix = ""
        var colName = name
        if (name.contains(".")) {
            val parts = name.split(".")
            dbSuffix = parts[0]
            colName = parts[1]
        }
        return getDb(dbSuffix).getCollection(colName)
    }

    fun uploadFile(inputStream: InputStream): Any {
        val gridFSBuckets = GridFSBuckets.create(db);
        return gridFSBuckets.uploadFromStream(UUID.randomUUID().toString(), inputStream)
    }

    //    fun getDbByCollectionName(name:String):MongoDatabase {
//        var dbSuffix = ""
//        var colName = name
//        if (name.contains(".")) {
//            val parts = name.split(".")
//            dbSuffix = parts[0]
//        }
//        return getDb(dbSuffix)
//    }
    private fun getDb(suffix: String): MongoDatabase {
        if (suffix.isBlank()) {
            return db;
        }
        val dbName = db.name + "-" + suffix
        return getDbByName(dbName)
    }

    fun getDbByName(dbName: String): MongoDatabase {
        if (!databasees.containsKey(dbName)) {
            databasees[dbName] = MongoDbClient.instance.getDatabase(dbName)
        }
        return databasees[dbName]!!
    }

    val db: MongoDatabase by lazy {
        val obj = MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))
        obj
    }

    val configDb: MongoDatabase by lazy {
        val obj = MongoDbClient.instance.getDatabase(db.name + "-configuration")
        obj
    }

    fun executeAggregateNonQuery(collectionName: String, pipeline: List<Document>) {
        db.getCollection(collectionName).aggregate(pipeline).allowDiskUse(true).toCollection()
    }

    fun dropCollection(collectionName: String) {
        db.getCollection(collectionName).drop()
    }

    fun clearCollection(collectionName: String) {
        db.getCollection(collectionName).deleteMany(Document())
    }

    fun getCollectionNames(): Iterable<String> {
        var list = db.listCollections()
        return list.map { it["name"].toString() }
    }


}