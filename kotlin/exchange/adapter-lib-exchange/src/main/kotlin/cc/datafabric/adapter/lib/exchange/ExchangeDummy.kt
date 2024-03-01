package cc.datafabric.adapter.lib.exchange

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import org.bson.Document

object ExchangeDummy {
    fun initialize(database: MongoDatabase? = null) {
        val db = database ?: ExchangeDatabase.db
        val col = db.getCollection(Names.Collections.dummy)
        val count = col.find().count()
        if (count == 1) {
            return
        }
        val doc = Document("_id" , "1")
        col.updateOne(doc, Document("\$set" , doc), UpdateOptions().upsert(true))
    }
}