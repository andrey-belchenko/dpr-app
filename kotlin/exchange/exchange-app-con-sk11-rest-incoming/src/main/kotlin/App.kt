package cc.datafabric.exchange.app.con.sk11.rest.incoming

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.ExchangeListener
import cc.datafabric.adapter.lib.exchange.ExchangeStore
import cc.datafabric.adapter.lib.sk.client.SkRestClient
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document


object App {
//todo см. Consumer

    private  val operationalTagsTask = Consumer.TaskDefinition(

        tag = "in_Markers",
        action = {

            val body = """
                         {
                            "statusFilter": "all",
                            "fromDateTime": "3000-01-01T01:00:00Z",
                            "toDateTime": "3000-01-01T01:00:00Z"
                         }
                    """
            return@TaskDefinition SkRestClient.post("operational-tags/v1.0/tags/query", body)
        }
    )

    private  val switchesTask = Consumer.TaskDefinition(
        tag = "in_Switches",
        action = {
            val ids = ExchangeStore.get("out_MonitoredSwitches").map { it["_id"] }.toList()
            if (ids.any()) {
                val bodyDoc = Document("switchUidBatch", ids)
                val body = bodyDoc.toJson()
                return@TaskDefinition SkRestClient.post("switches/v2.0/switches/statuses/get-at-time-moment", body)
            }else{
                return@TaskDefinition ""
            }
        },
        resultCleaner = {
            BsonUtils.getDocumentsRecursive(it).toList().forEach {docInfo->
                BsonUtils.getProperties(docInfo.document).toList().forEach {propInfo->
                    if (propInfo.name=="instance"){
                        docInfo.document.remove(propInfo.name)
                    }
                }
            }
            return@TaskDefinition it
        }
    )

    private  val nodeStatusTask = Consumer.TaskDefinition(
        action = {
            ExchangeListener.getChanges(listOf("out_skNodeStatusRequest")){requests->
                Logger.workStarted()
                requests.changedCollections.forEach {coll->
                    coll.getDocuments().forEach {doc->
                        val bodyDoc = Document("connectivityNodeUidBatch", doc["nodeIds"])
                        val body = bodyDoc.toJson()
                        val response =  SkRestClient.post("topology/v2.0/actual/connectivity-nodes/get", body)
                        val responseDoc = parse(response)
                        val msgDoc = Document()
                        msgDoc["requestInfo"] =  doc["requestInfo"]
                        msgDoc["response"] = responseDoc
                        ExchangeStore.put("in_skNodeStatusResponse", msgDoc)
                        Logger.traceData(response)
                    }
                }
                Logger.workFinished()
            }
            return@TaskDefinition "ок"

        },
        defaultResultProcessing= false,
    )

    fun main() {
        Consumer.consume(
            listOf(
                operationalTagsTask,
                switchesTask,
                nodeStatusTask
            )
        ) { taskResult ->
            if (taskResult.value!=""){
                val doc = parse(taskResult.value)
                ExchangeStore.put(taskResult.taskDefinition.tag, doc)
                Logger.traceData(taskResult.value)
            }
        }
    }

    private fun parse(value: String):Document{
        try {
            return  Document.parse(value)
        } catch (e: Exception) {
            throw Exception("Can't parse response. ${e.message}\n$value")
        }
    }
}
