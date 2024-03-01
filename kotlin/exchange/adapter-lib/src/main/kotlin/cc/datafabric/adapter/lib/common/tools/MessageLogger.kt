package cc.datafabric.adapter.lib.common.tools

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.general.IncomingOutMessage
import cc.datafabric.adapter.lib.general.OutgoingInMessage
import cc.datafabric.adapter.lib.general.OutgoingOutMessage
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import com.mongodb.*
import com.mongodb.client.MongoClients

object MessageLogger {

    private val mongoClient by lazy {


        Stopwatch.start("mongo init")
        val uri = Config.get("adp_mongo_uri")
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .serverApi(serverApi)
            .build()
        val res =  MongoClients.create(settings)
        Stopwatch.stop("mongo init")
        res
    }



    private val servDb by lazy { mongoClient.getDatabase(Config.get("adpMongoServDb"))}
    private val messageLogCollection by lazy { servDb.getCollection("messageLog")}

    fun isDisabled():Boolean{
        return Config.get("adpMessageLoggingEnabled") != "true"
    }
    fun log(message: IncomingOutMessage, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        log(message.getText(), messageType,extraInfo)
    }

    fun log(message: OutgoingInMessage, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        log(message.getText(), messageType,extraInfo)
    }
    fun log(message: OutgoingOutMessage, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        log(message.getText(), messageType,extraInfo)
    }
    fun log(message: String, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        Logger.traceFunBeg()
        val doc = Document()
        doc["message"] = message
        doc["timestamp"] = BsonUtils.getCurrentTimeStamp()
        doc["messageType"] = messageType
        doc["appName"]=Config.get("adpName")

        extraInfo?.forEach { (key, value) ->
            doc[key]=value
        }


        Stopwatch.start("log message")
        messageLogCollection.insertOne(doc)
        Stopwatch.stop("log message")
        customProcessor(MessageInfo(message,messageType))
        Logger.traceFunEnd()
    }

    class MessageInfo (val message: String, val messageType: String)
    private var customProcessor:(MessageInfo)->Unit = {}
    fun setCustomProcessor(customProcessor:(MessageInfo)->Unit){
        this.customProcessor=customProcessor
    }

}