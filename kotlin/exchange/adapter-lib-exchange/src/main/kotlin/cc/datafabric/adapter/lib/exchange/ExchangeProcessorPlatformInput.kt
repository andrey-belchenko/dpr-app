package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import org.bson.Document

object ExchangeProcessorPlatformInput {
    fun processPlatformInput(batchId: String, operationId: String, flow: ExchangeFlow) {
        val filter = Document(Names.Fields.operationId, operationId)
        val docs = ExchangeDatabase.db.getCollection(Names.Collections.modelInput).find(filter)
        val keyMap = getKeys(operationId)

//        val changedAt = ExchangeTimestamp.now()
        val outDocs = mutableListOf<Document>()
        docs.forEach { rootDoc ->
            if (rootDoc[Names.Fields.verb] != null) {
                BsonUtils.getDocumentsRecursiveFromValue(rootDoc[Names.Fields.model]).forEach { objInfo ->
                    val obj = objInfo.document
                    val idSource = (obj[Names.Fields.atIdSource]?.toString() ?: rootDoc[Names.Fields.idSource]?.toString())!!

                    val sourceId = obj[Names.Fields.atId].toString()
                    val platformId = keyMap.getPlatformKey(idSource,sourceId)
                    obj[Names.Fields.atId] = platformId
                }
//            rootDoc[Names.Fields.noun] = flow.noun
//            rootDoc[Names.Fields.verb] = flow.verb
//            rootDoc[Names.Fields.messageType] = flow.messageType
//                rootDoc[Names.Fields.changedAt] = changedAt
                outDocs.add(rootDoc)
            }

        }
        if (outDocs.any()) {
            //todo ограничить порцию
            ExchangeStore.insertWithTimestamp(Names.Collections.platformInput,outDocs)
            CollectionChangeInfoStore.set(Names.Collections.platformInput)
        }
    }

    private fun getKeys(operationId: String):KeyMap{
        val pipeline = listOf(
            Document("\$match", Document(Names.Fields.operationId,operationId)),
            Document("\$replaceRoot",Document("newRoot","\$${Names.Fields.extId}"))
        )
        val keys = ExchangeDatabase.db.getCollection(Names.Collections.modelEntities).aggregate(pipeline)
        return  KeyMap(keys)
    }

    class KeyMap(val data:Iterable<Document>){
        private val cache = mutableMapOf<String,Map<String,String>>()
        fun getPlatformKey(idSource: String, key:String): String {
            if (!cache.containsKey(idSource)){
                val map = mutableMapOf<String,String>()
                data.forEach {
                    val sourceId = it[idSource].toString()
                    val platformId = it[Names.Values.platform].toString()
                    map[sourceId]=platformId
                }
                cache[idSource] = map
            }
            return cache[idSource]!![key]!!
        }
    }
}