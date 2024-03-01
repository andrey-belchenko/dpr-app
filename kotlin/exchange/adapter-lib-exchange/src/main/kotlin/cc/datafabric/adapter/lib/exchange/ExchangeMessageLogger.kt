package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
//import cc.datafabric.adapter.lib.exchange.general.IncomingOutMessage
import cc.datafabric.adapter.lib.exchange.general.OutgoingInMessage
//import cc.datafabric.adapter.lib.exchange.general.OutgoingOutMessage
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.sys.ConfigNames
object ExchangeMessageLogger {


    private val servDb by lazy { ExchangeDatabase.db}
    private val messageLogCollection by lazy { servDb.getCollection(Names.Collections.messageLog)}

    private var suppressed = false

    fun suppress(){
        suppressed = true
    }

    fun resume(){
        suppressed = false
    }

    private fun isDisabled():Boolean{
        return suppressed ||  Config.get("adp_message_logging_enabled") != "true"
    }
//    fun log(message: IncomingOutMessage, messageType: String, extraInfo:Map<String,Any?>?=null) {
//        if (isDisabled()) return
//        log(message.getText(), messageType,extraInfo)
//    }

    fun log(message: OutgoingInMessage, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        log(message.getText(), messageType,extraInfo)
    }
//    fun log(message: OutgoingOutMessage, messageType: String, extraInfo:Map<String,Any?>?=null) {
//        if (isDisabled()) return
//        log(message.getText(), messageType,extraInfo)
//    }

    fun log(messages: Iterable<Document>, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        val doc= Document("data",messages.toList())
        log(BsonUtils.toJsonString(doc), messageType,extraInfo)
    }

    fun log(message: Document, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return

        log(BsonUtils.toJsonString(message), messageType,extraInfo)
    }



    fun log(message: String, messageType: String, extraInfo:Map<String,Any?>?=null) {
        if (isDisabled()) return
        var text = message
        if (message.length>100000){
            text = "Message is too large"
        }
        Logger.traceFunBeg()
        val doc = Document()
        doc["message"] = text
        doc[Names.Fields.changedAt] = ExchangeTimestamp.now()
        doc["messageType"] = messageType
        doc[Names.Fields.service]=Config.get(ConfigNames.processorName)
        extraInfo?.forEach { (key, value) ->
            doc[key]=value
        }
        messageLogCollection.insertOne(doc)
        CollectionChangeInfoStore.set(Names.Collections.messageLog)
        Logger.traceFunEnd()
    }



}