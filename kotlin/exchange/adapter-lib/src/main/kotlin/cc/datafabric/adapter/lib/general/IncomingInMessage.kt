package cc.datafabric.adapter.lib.general

import org.bson.Document

/**
 * Incoming message in special profile intended for transformation with aggregation pipeline
 */
class IncomingInMessage (val value:String) {
    private val document = Document.parse(value)
    fun asObject(): Document {
        return document
    }
    fun getEventName(): String {
        // todo хардкод
        return document["КодСобытия"].toString()
    }




}