package cc.datafabric.exchange.app.con.meter.incoming

import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.model.Indexes
import org.bson.Document
import java.util.*


object App {
    private val timer = ExchangeTimer(10000)
    fun main() {
        ExchangeStore.useTimer = false
        timer.run {
            if (!ExchangeStatusStore.getProcessorDisabled()){
                ExchangeStore.inProgress = true
                action()
                ExchangeStore.inProgress = false
            }
        }
    }

    private const val portionSize = 1000
    private fun action() {

        ExchangeDatabase.getCollection(Names.Collections.scheduledActions).createIndex(
            Indexes.ascending(
                listOf(
                    "date",
                    Names.Fields.service,
                    "isCompleted"
                )
            )
        )
        val tasks = ExchangeDatabase.getCollection(Names.Collections.scheduledActions).find(
            Document(
                mapOf(
                    "date" to Document("\$lt", ExchangeTimestamp.now()),
                    Names.Fields.service to Config.get(ConfigNames.processorName),
                    "isCompleted" to false,
                )
            )
        ).toList()
        if (tasks.any()) {
            ExchangeStatusStore.setProcessorStatusProcessing()
            Logger.workStarted()
        }
        tasks.forEach { task ->
            ExchangeStatusStore.setProcessorProgress("")
            val taskId = task["_id"]!!
            val params = task["params"] as Document
            val start = params.getDate("start")
            val end = params.getDate("end")
            var portionIndex = task.getInteger("portionIndex") ?: 0
            var doNext = true
            var total = 0
            var progressMessage = ""
            var filter = params["filter"] as? Document
            var currentStage = task.getString("stage") ?: "accountUsagePoints"
            // обросло костылями сначала было просто usagePoints, потом разделил на accountUsagePoints и supplyUsagePoints

            val itemType = params.getString("itemType")

            if (filter == null) {
                filter = Document()
            }
            if (currentStage == "accountUsagePoints" && listOf("usagePoint", "accountUsagePoint").contains (itemType ?: "accountUsagePoint") ) {
                while (doNext) {
                    val actualCount = loadUsagePointPortion(start, end, portionIndex, filter, false)
                    doNext = actualCount > -1  // actualCount  == portionSize
                    if (doNext) {
                        total += actualCount
                        portionIndex++
                        Logger.status("Loaded account usage points count: $total")
                        progressMessage =
                            "stage:$currentStage, portions:$portionIndex, count: $total"
                        ExchangeStatusStore.setProcessorProgress(progressMessage)
                        updateTaskProgress(taskId, currentStage, portionIndex)
                    }
                }
            }

            if (currentStage=="accountUsagePoints") {
                currentStage = "supplyUsagePoints"
            }
            portionIndex = 0
            doNext = true
            total = 0
            if (currentStage == "supplyUsagePoints" && listOf("usagePoint", "supplyUsagePoint").contains (itemType ?: "supplyUsagePoint") ) {
                while (doNext) {
                    val actualCount = loadUsagePointPortion(start, end, portionIndex, filter, true)
                    doNext = actualCount > -1  // actualCount  == portionSize
                    if (doNext) {
                        total += actualCount
                        portionIndex++
                        Logger.status("Loaded supply usage points count: $total")
                        progressMessage =
                            "stage:$currentStage, portions:$portionIndex, count: $total"
                        ExchangeStatusStore.setProcessorProgress(progressMessage)
                        updateTaskProgress(taskId, currentStage, portionIndex)
                    }
                }
            }

            currentStage = "meters"
            portionIndex = 0
            doNext = true
            total = 0
            if ( (itemType ?: "meter") == "meter") {
                while (doNext) {
                    val actualCount = loadMeterPortion(start, end, portionIndex, filter)
                    doNext = actualCount > -1  // actualCount  == portionSize
                    if (doNext) {
                        total += actualCount
                        portionIndex++

                        Logger.status("Loaded meters count: $total")
                        progressMessage = "stage:$currentStage, portions:$portionIndex, count: $total"
                        ExchangeStatusStore.setProcessorProgress(progressMessage)
                        updateTaskProgress(taskId, currentStage, portionIndex)
                    }

                }
            }

            ExchangeDatabase.getCollection(Names.Collections.scheduledActions).updateOne(
                Document("_id", taskId),
                Document(
                    "\$set", Document(
                        mapOf(
                            "isCompleted" to true,
                            "competedAt" to ExchangeTimestamp.now()
                        )
                    )
                )
            )
        }
        if (tasks.any()) {
            Logger.workFinished()
            ExchangeStatusStore.setProcessorCompletionTime()
            ExchangeStatusStore.setProcessorStatusWaiting()
        }
    }

    private  fun updateTaskProgress (_id:Any,stage:String,portionIndex:Int) {
        ExchangeDatabase.getCollection(Names.Collections.scheduledActions).updateOne(
            Document("_id", _id),
            Document(
                "\$set", Document(
                    mapOf(
                        "stage" to stage,
                        "portionIndex" to portionIndex
                    )
                )
            )
        )
    }

    private fun loadUsagePointPortion(start: Date, end: Date, portionIndex:Int, filter:Document, isSdp:Boolean) :Int {
        val offset = portionIndex * portionSize
        val result = ApiClient.getUsagePoints(start, end, offset, portionSize,isSdp)
        val extraInfo = mapOf(
            "start" to start,
            "end" to end,
            "portionIndex" to portionIndex
        )
        ExchangeMessageLogger.log(result, "in", extraInfo)
        val allDocs = ResponseParser.parse(result, "usagePoint")
        if (!allDocs.any()){
             // todo костыль, -1 используется для обозначения того что прочитана последняя порция
             return -1
        }
        val docs = filterDocs(allDocs, filter)
        saveItemsToDb(docs, "usagePoint")
        return docs.count()
    }

    private fun loadMeterPortion(start: Date, end: Date, portionIndex:Int, filter:Document) :Int {
        val offset = portionIndex * portionSize
        val result = ApiClient.getMeters(start, end, offset, portionSize)
        val extraInfo = mapOf(
            "start" to start,
            "end" to end,
            "portionIndex" to portionIndex
        )
        ExchangeMessageLogger.log(result, "in", extraInfo)
        val allDocs = ResponseParser.parse(result, "meter")
        if (!allDocs.any()){
            // todo костыль, -1 используется для обозначения того что прочитана последняя порция
            return -1
        }
        val docs = filterDocs(allDocs, filter)
        saveItemsToDb(docs, "meter")
        return docs.count()
    }

    private fun filterDocs(docs:Iterable<Document>, filter:Document) :Iterable<Document> {
        var filteredDocs = docs
        filter.keys.forEach { fieldPath ->
            val filterValue =  (filter[fieldPath] as List<*>)
            filteredDocs =  filteredDocs.filter { filterValue.contains(getFieldValueByPath(it,fieldPath)) }
        }
        return filteredDocs
    }

    private fun getFieldValueByPath(doc: Document, path: String): Any? {
        val keys = path.split(".")
        var tempDoc: Document? = doc

        for (key in keys) {
            if (tempDoc?.containsKey(key) == true) {
                val value = tempDoc[key]
                if (value is Document) {
                    tempDoc = value
                } else {
                    return value
                }
            } else {
                return null
            }
        }

        return null
    }
    private fun saveItemsToDb(items: Iterable<Document>, itemType: String) {
        Logger.traceFun {
            items.forEach { item ->
                item["code"] = item["mRID"]
                val doc = Document(
                    mapOf(
                        "payload" to Document(
                            mapOf(
                                "verb" to itemType,
                                "source" to "КИСУР",
                                "body" to item
                            ),
                        ),
                        "messageId" to UUID.randomUUID().toString(),
                        "dtoId" to "$itemType:${item["code"]}",
                        "eventId" to itemType,
                        "objectId" to item["code"]
                    )
                )
                ExchangeStore.put("in_$itemType", doc)
                ExchangeStore.put(Names.Collections.incomingMessages, doc)
            }
            ExchangeStore.applyInsert()
        }
    }
}
