package cc.datafabric.exchange.app.con.sk11.nodes.incoming

import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.sk.client.SkRestClient
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import kotlin.system.measureTimeMillis


object App {
    private const val idsCollectionName = "out_MonitoredNodes"
    private const val resultCollectionName = "flow_NodesStatus"
    private const val inputCollectionName = "in_NodesStatus"
    private val timer = ExchangeTimer(Config.get(ConfigNames.skConsumingIntervalMs).toLong())
    private val lastResult = mutableMapOf<String,Document>()
    private val ids = mutableSetOf<String>()
    private var isDisabled = false
    fun main() {
        timer.run {
            val newIsDisabled = ExchangeStatusStore.getProcessorDisabled()
            if (newIsDisabled != isDisabled) {
                reset()
                isDisabled = newIsDisabled
            }

            if (!isDisabled) {
                val initTime = measureTimeMillis {
                    initializeIfNeed()
                }
                val updIdsTime = measureTimeMillis {
                    updateMonitoredIds()
                }
                var newResult: Iterable<Document>
                if (ids.any()) {
                    val queryTime = measureTimeMillis {
                        newResult = getCurrentNodeStatuses()
                    }
//            Logger.traceData("newResult:")
//            newResult.forEach {
//                Logger.traceData(BsonUtils.toJsonString(it))
//            }
                    var changedItems: Iterable<Document>
                    val compareTime = measureTimeMillis {

                        changedItems = findChangedItems(newResult)
                    }

                    if (changedItems.any()) {
                        ExchangeMessageLogger.suppress()
                        val inputTime = measureTimeMillis {
                            applyChangedItems(changedItems)
                        }
                        ExchangeMessageLogger.resume()
                        val fullTime = initTime + updIdsTime + queryTime + compareTime + inputTime
                        val info = Document(
                            mapOf(
                                "count" to changedItems.count(),
                                "initTime" to initTime,
                                "updIdsTime" to updIdsTime,
                                "queryTime" to queryTime,
                                "compareTime" to compareTime,
                                "inputTime" to inputTime,
                                "fullTime" to fullTime,
                            )
                        )
                        ExchangeMessageLogger.log("", "info", mapOf("info" to info))
                    }
                } else {
                    Logger.status("CANCEL REQUEST. Reason: there is no monitored nodes")
                }
            }

        }
    }

    private fun loadNodesStatus() {
        Logger.traceFun {
            val docs = ExchangeDatabase.getCollection(resultCollectionName).find()
            docs.forEach { doc ->
                lastResult[doc["_id"].toString()] = doc
            }
        }
    }
    private fun loadNodesIds() {
        Logger.traceFun {
            val docs = ExchangeDatabase.getCollection(idsCollectionName).find()
            docs.forEach { doc ->
                addId(doc["_id"].toString())
            }
        }
    }

    private fun addId(id:String){
        if (!ids.contains(id)){
            ids.add(id)
        }
    }

    private var initialized:Boolean = false
    private fun initializeIfNeed() {
        Logger.traceFun {
            if (!initialized) {
                initialize()
            }
            initialized = true
        }
    }

    private fun reset() {
        ids.clear()
        lastResult.clear()
        initialized = false
    }

    private fun initialize() {
        Logger.workStarted()
        Logger.traceFun {
            loadNodesIds()
            loadNodesStatus()
        }
        Logger.workFinished()
    }

    private fun applyNewMonitoredIds(collection:ExchangeListener.ChangedCollection) {
        Logger.workStarted()
        Logger.traceFun {
            collection.getDocuments().forEach { doc->
                addId(doc["_id"].toString())
            }
        }
        Logger.workFinished()
    }

    private fun updateMonitoredIds() {
        Logger.traceFun {
            ExchangeListener.getChanges(listOf(idsCollectionName)){result->
                result.changedCollections.forEach {coll->
                    applyNewMonitoredIds(coll)
                }
            }
        }
    }


    private fun getCurrentNodeStatuses():Iterable<Document>{
        return Logger.traceFun {
            val bodyDoc = Document("connectivityNodeUidBatch", ids)
            val body = bodyDoc.toJson()
            val response = SkRestClient.post("topology/v2.0/actual/connectivity-nodes/get", body)
            val responseDoc = parse(response)
            // Если узел не найден по нему возвращается такой ответ
//            {
//                "connectivityNodeUid": "1ab3953a-d03f-4f37-85f0-433301177e75",
//                "error": {
//                "type": "urn:monitel:problemdetails:ck11:topology:topology-error",
//                "title": "Topology error",
//                "status": 404,
//                "detail": "Connectivity node 1ab3953a-d03f-4f37-85f0-433301177e75 not found.",
//                "instance": "urn:monitel:problemdetails:instance:ck11:topology:00-de60b9977a154f48aa2c87cfffea8621-afec2340920e1a40-00"
//            }
//            }
            //todo ввести ошибки в список предупреждений коллекция sys_Warnings
            return@traceFun (responseDoc["value"] as Iterable<Document>).filter { it ["error"] == null }
        }
    }

    private  fun isItemsEqual(oldItem:Document, newItem:Document):Boolean{
        val oldValue =  oldItem["value"] as Document
        val newValue =  newItem["value"] as Document
        return oldValue["energized"] == newValue["energized"]
                && oldValue["grounded"] == newValue["grounded"]
                && oldValue["nominalVoltage"] == newValue["nominalVoltage"]
    }
    private fun findChangedItems(newResult:Iterable<Document>):Iterable<Document>{
        return Logger.traceFun {
            val changedItems = mutableListOf<Document>()
            newResult.forEach { newItem->
                val id  = newItem["connectivityNodeUid"].toString()
                val oldItem= lastResult[id]
//                Logger.traceData("oldItem:")
//                Logger.traceObject(oldItem)
//                Logger.traceData("newItem:")
//                Logger.traceObject(newItem)

                if (oldItem ==  null || !isItemsEqual(oldItem,newItem)){
                    changedItems.add(newItem)
                }
            }
            return@traceFun changedItems
        }
    }

    private fun updateLastResult(changedItems:Iterable<Document>){
        Logger.traceFun {
            changedItems.forEach {doc->
                lastResult[doc["connectivityNodeUid"].toString()] = doc
            }
        }
    }

    private fun saveChangedItemsToDb(changedItems:Iterable<Document>){
        Logger.traceFun {
            changedItems.forEach {doc->
                ExchangeStore.put(inputCollectionName,doc)
            }
            ExchangeStore.applyInsert()
        }
    }
    private fun applyChangedItems(changedItems:Iterable<Document>){
        Logger.traceFun {
            updateLastResult(changedItems)
            saveChangedItemsToDb(changedItems)
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
