package cc.datafabric.exchange.app.con.rabbit.outgoing

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.rabbit.RabbitProducer
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object App {
    fun main() {
        ExchangeStatusStore.setProcessorStatusWaiting()
        ExchangeListener.listenCollections(listOf("out_Rgis")) { res ->

            res.changedCollections.forEach { col ->
                val extraInfo = mapOf<String, Any>(
                    "startTimestamp" to res.lastTimestamp,
                    "endTimestamp" to res.newTimestamp,
                )
                val docs = col.getDocuments().toList()
                ExchangeMessageLogger.log(docs, "in", extraInfo)

                docs.forEach {document->
                    processDocument(document,extraInfo)
                }
            }
        }
    }
    private fun processDocument(document: Document, extraInfo: Map<String, Any>){
        Logger.traceMassiveFun {
            Logger.traceObject(document)
            val queueName = document["queueName"].toString()
            val message =  document[Names.Fields.payload] as Document
            message["ИдСообщения"] =  UUID.randomUUID().toString()
            BsonUtils.getDocumentsRecursive(message).toList().forEach { doc ->
                BsonUtils.getProperties(doc.document).filter { it.value is Date }.toList().forEach {prop->
//                    val value = prop.value as Date
//                    val localOffsetDate  = value.toInstant().atOffset(OffsetDateTime.now().offset)
//                    val  utcOffsetDate =   localOffsetDate.withOffsetSameInstant(ZoneOffset.UTC)
                    prop.document[prop.name] =   BsonUtils.valueToString(prop.value)
                }
            }
            val extInfo =  extraInfo.toMutableMap()
            extInfo["queueName"] = queueName
            val messageText =  BsonUtils.toJsonString(message)
            RabbitProducer.produce(queueName, messageText)
            ExchangeMessageLogger.log(messageText, "out", extInfo)
        }
    }

}