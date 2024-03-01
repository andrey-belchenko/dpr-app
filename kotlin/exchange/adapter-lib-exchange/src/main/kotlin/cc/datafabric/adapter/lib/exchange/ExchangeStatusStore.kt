package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.TimeUtils.toFormattedString
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.exchange.common.LoggerExtension.traceObject
import java.time.ZonedDateTime

import cc.datafabric.adapter.lib.sys.ConfigNames
import com.mongodb.client.model.Field
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions

object ExchangeStatusStore {

    private const val collectionName = "sys_StatusStore"


    private val collection by lazy {
        val db= ExchangeDatabase.db
        val col = db.getCollection(collectionName)

        IndexManager.create(collectionName,false, listOf(Names.Fields.service, "varName"))

        col
    }

//    init {
//        val version = "2023-06-04"
//        Logger.status("version:$version")
//        set("version", version) //todo для отладки
//    }
    fun get(name:String):Any? {
        return Logger.traceFun (name) {
            val params = Document()
            params[Names.Fields.service] = Config.get(ConfigNames.processorName)
            params["varName"] = name
            val result = collection.find(params).firstOrNull()
            Logger.traceObject(result)
            if (result==null){
                return@traceFun null
            }
            return@traceFun result["value"]
        }
    }

    fun set(name:String, value:Any ){
        Logger.traceFun (name,value) {
            val doc = Document()
            doc["varName"] = name
            doc[Names.Fields.service] = Config.get(ConfigNames.processorName)
            doc["value"] = value
            doc[Names.Fields.timestamp] = ExchangeTimestamp.now()

            val filter = Document()
            filter[Names.Fields.service] = Config.get(ConfigNames.processorName)
            filter["varName"] = name
            Logger.traceObject(doc)
            collection.replaceOne(filter,doc, ReplaceOptions().upsert(true))
        }
    }

    fun set(name:String,value: ZonedDateTime) {
        set(name,value.toFormattedString())
    }

    fun getInt(name:String):Int? {
        val value = get(name) ?: return  null
        return value as Int?
    }
//
//    fun getZonedDateTime(name:String): ZonedDateTime?{
//        val value = get(name) ?: return  null
//        return TimeUtils.parseZonedDateTimeString(value.toString())
//    }

//    private fun remove(params:Document){
//        Logger.traceFun {
//            Logger.traceObject(params)
//            collection.deleteOne(params)
//        }
//    }

    private var oldDisabled =  false
    fun getProcessorDisabled():Boolean {
        val value = get(Names.Fields.processorDisabled) as Boolean? ?: false
        if (value ||  oldDisabled!=value){
            Logger.workStarted()
            Logger.status(if (value) "DISABLED" else "ENABLED")
            Logger.workFinished()
        }
        oldDisabled = value
        return value
    }

    fun getProcessorStatus():String? {
        return get(Names.Fields.processorStatus)?.toString()
    }
    fun setProcessorStatusWaiting() {
        set(Names.Fields.processorStatus, Names.Values.listening)
    }

    fun setProcessorStatusProcessing() {
        set(Names.Fields.processorStatus, Names.Values.processing)
    }

    fun isProcessorStatusProcessing():Boolean {
        return  getProcessorStatus() == Names.Values.processing
    }

    fun isProcessorStatusWaiting():Boolean {
        return  getProcessorStatus() == Names.Values.listening
    }

    fun setProcessorStatusError() {
        set(Names.Fields.processorStatus, Names.Values.error)
    }

    fun setProcessorCompletionTime() {
        set(Names.Fields.completedTimestamp, ExchangeTimestamp.now())
    }

    fun setProcessorProgress(value: String) {
        set(Names.Fields.processorProgress, value)
    }

    fun setProcessorErrorTime() {
        set(Names.Fields.errorTimestamp, ExchangeTimestamp.now())
    }
    class FlowStatesTags {
        var disabledFlowsTags:Iterable<String> = setOf<String>()
        var enabledFlowsTags:Iterable<String> = setOf<String>()
    }

//    fun setFlowStatesTags (disabledFlowsTags:Iterable<String>,enabledFlowsTags:Iterable<String> ){
//        val doc = Document(mapOf(
//            "disabled" to disabledFlowsTags,
//            "enabled" to enabledFlowsTags
//        ))
//        set("flowStatesTags",doc)
//    }

    fun getFlowStatesTags () :FlowStatesTags{
        val setting = get("flowStatesTags")
        val value = FlowStatesTags()
        if (setting is Document){
            value.disabledFlowsTags = (setting["disabled"] as List<*>).map { it.toString() }.toSet()
            value.enabledFlowsTags = (setting["enabled"] as List<*>).map { it.toString() }.toSet()
        }
        return value
    }

}