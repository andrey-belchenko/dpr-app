package cc.datafabric.adapter.lib.exchange.general

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document

/**
 * Message in common profile received from platform. It is intended to be transformed and sent to the external system
 */

class OutgoingInMessage (val value:String) {
    //todo рассмотреть вынос общих методов для сообщений в базовый класс
    private val document = Document.parse(value)
    fun getVerb(): String? {
        val value = document["m:Verb"]
        if (value is String) {
            return value
        }
        return null
    }
    fun getType(): String? {
        val value = document["@type"]
        if (value is String) {
            return value.split(":").last()
        }
        return null
    }

    fun getText():String {
        Logger.traceFunBeg()
        val value = BsonUtils.toJsonString(document)
        Logger.traceFunEnd()
        return value
    }
    internal fun asObject() = document
//    //todo временное решение, уточнить формат сообщения, доработать
//    fun getPayloadAsString(): String {
//        val buff= Document.parse(value)
//        buff.remove("m:Verb")
//        return BsonUtils.toJsonString(buff)
//    }
}