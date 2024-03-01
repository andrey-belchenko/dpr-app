package cc.datafabric.adapter.lib.general

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document

/**
 * Message in common profile received from platform. It is intended to be transformed and sent to the external system
 */
class OutgoingOutMessage (private val document:Document) {
    //todo рассмотреть вынос общих методов для сообщений в базовый класс
    companion object {
        internal fun fromPipelineResult(value: Document): OutgoingOutMessage {
            value.remove("_id")
            return OutgoingOutMessage(value)
        }
        fun fromString(value: String): OutgoingOutMessage {
            return OutgoingOutMessage(Document.parse(value))
        }
    }

    fun getEventName(): String {
        // todo хардкод
        return document["КодСобытия"].toString()
    }

    fun getEntities():Sequence<OutgoingOutMessageEntity> = sequence {
        BsonUtils.getDocumentsRecursiveFromValue(getPayload()).forEach {
            yield(OutgoingOutMessageEntity(this@OutgoingOutMessage,it.document))
        }
    }
    // todo хардкод
    internal fun getPayload()=document["Тело"]

    fun getText():String {
        Logger.traceFunBeg()
        val value = BsonUtils.toJsonString(document)
        Logger.traceFunEnd()
        return value
    }
//    fun getVerb(): String? {
//        val value = document["m:Verb"]
//        if (value is String) {
//            return value
//        }
//        return null
//    }
//    fun getType(): String? {
//        val value = document["@type"]
//        if (value is String) {
//            return value.split(":").last()
//        }
//        return null
//    }
//
//    internal fun asObject() = document
//    //todo временное решение, уточнить формат сообщения, доработать
//    fun getPayloadAsString(): String {
//        val buff= Document.parse(value)
//        buff.remove("m:Verb")
//        return BsonUtils.toJsonString(buff)
//    }
}