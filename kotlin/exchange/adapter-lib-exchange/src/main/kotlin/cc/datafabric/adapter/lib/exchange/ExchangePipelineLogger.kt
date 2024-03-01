package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.common.MongoDbClient
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.sys.ConfigNames
object ExchangePipelineLogger {

    private val db by lazy {
        val obj = MongoDbClient.instance.getDatabase(Config.get("adp_mongo_exchange_db"))
        obj
    }

    private val logCollection by lazy { db.getCollection(Names.Collections.pipelineLog)}

    private fun isDisabled():Boolean{
        return Config.get(ConfigNames.pipelineLoggingEnabled)!="true"
    }

    fun any():Boolean {
        return logCollection.countDocuments() > 0
    }
    fun getPipelineText(source: String, pipeline: List<Document>?):String? {
        if (pipeline==null){
            return  null
        }
        val sb =  StringBuilder()
        sb.appendLine("db.getCollection(\"$source\").aggregate([")
        pipeline.forEach {
            sb.appendLine(BsonUtils.toJsonString(it) +",")
        }
        sb.appendLine("])")
        return sb.toString()
    }
//    fun log(input: String, output:String, pipeline: List<Document>?, src:String?, count:Long,  batchId:String,operationId:String,sysAction:String?,errorInfo:Document?=null) {
//        if (isDisabled()) return
//        //todo производительность?
//        log (input, output, pipeline, src, count, batchId,operationId, sysAction,errorInfo)
//    }

    fun log(input: String, output:String, pipeline: List<Document>?, src:String?, count:Long, batchId:String,operationId:String,sysAction:String?,timeMs:Long?,errorInfo:Document?=null) {
        Logger.traceFun {
            if (isDisabled()) return@traceFun
            val doc = Document()
            if (errorInfo!=null){
                doc["error"] = errorInfo
            }
            doc["src"] = src
            doc[Names.Fields.changedAt] = BsonUtils.getCurrentTimeStamp()
            doc[Names.Fields.batchId] = batchId
            doc[Names.Fields.operationId] = operationId
            doc[Names.Fields.sysAction] = sysAction
            doc["input"] = input
            doc["output"] = output
            doc["timeMs"] = timeMs
            doc["count"] =  count
            doc["pipeline"] = getPipelineText(input, pipeline)
            doc[Names.Fields.processorName] = Config.get(ConfigNames.processorName)
            logCollection.insertOne(doc)
            CollectionChangeInfoStore.set(Names.Collections.pipelineLog)
        }
    }


}