package cc.datafabric.adapter.lib.exchange
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.common.MongoDbClient
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*

object ExchangeErrorLogger {

    private val db by lazy {
        val obj = MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))
        obj
    }

    private val logCollection by lazy { db.getCollection("sys_Errors")}


    fun log(message: String?, stackTrace:String) {
        val doc = Document()
        doc["appName"] = Config.get(ConfigNames.processorName)
        doc["timestamp"] = BsonUtils.getCurrentTimeStamp()
        doc["message"] = message
        doc["stackTrace"] = stackTrace
        logCollection.insertOne(doc)
    }


}