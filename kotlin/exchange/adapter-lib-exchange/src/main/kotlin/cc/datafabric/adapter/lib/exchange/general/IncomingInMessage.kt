package cc.datafabric.adapter.lib.exchange.general

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import org.bson.Document

/**
 * Incoming message in special profile intended for transformation with aggregation pipeline
 */
class IncomingInMessage (var document: Document)
{
    constructor(value:String) : this(Document.parse(value)) {

    }

    fun asObject(): Document {
        return document
    }
    fun getEventName(): String {

        // todo: переходный вариянт
        var fEvent = "КодСобытия"
        if (document[fEvent] == null){
            fEvent = "verb"
        }
        // todo хардкод
        return document[fEvent].toString()
    }
    fun getRootId(): String? {


        // todo: переходный вариянт
        var fBody = "Тело"
        var fCode = "КодТехническогоОбъекта"
        var fEl = "ЭлементСтруктурыСети"

        if (document["Тело"] == null) {
            fBody = "body"
            fCode = "code"
            fEl = "element"
        }


        // todo хардкод
        // не проверено
        var element = document[fBody]
        if (element is Document){
            if (element[fCode]==null){
                 element = element[fEl]
            }
        }
        if (element is Document){
            return element[fCode]?.toString()
        }
        return  null
    }
}