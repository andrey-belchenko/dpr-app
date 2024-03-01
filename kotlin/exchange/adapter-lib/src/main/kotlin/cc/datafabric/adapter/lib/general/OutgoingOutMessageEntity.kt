package cc.datafabric.adapter.lib.general

import cc.datafabric.adapter.lib.common.BsonUtils
import org.bson.Document
/**
 * Entity (JSON object, document) in the message payload
 */

class OutgoingOutMessageEntity (val message: OutgoingOutMessage, val obj: Document) {

    fun setKey(value: String) {
        // todo хардкод
        obj["КодТехническогоОбъекта"] = value
    }

    fun getKey(): String? {
        // todo хардкод
        if (obj["КодТехническогоОбъекта"]==null){
            return  null
        }
        return obj["КодТехническогоОбъекта"] as String
    }

    fun toJsonString():String{
        return BsonUtils.toJsonString(obj)
    }

}