package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import org.bson.Document

object ExchangeProcessorMessageInput {
    fun processMessageInput(batchId:String,operationId:String) {

        //todo, переделать управление этими параметрами, может быть сделать параметром функции put
        ExchangeStore.useTimer = false
        ExchangeStore.doMessageLogging = false
        val filter = Document(Names.Fields.operationId, operationId)
        val docs = ExchangeDatabase.db.getCollection(Names.Collections.messageInput).find(filter)
        docs.forEach {doc->
            ExchangeStore.put(doc[Names.Fields.payload] as Document)
        }
        ExchangeStore.applyInsert()
    }
}