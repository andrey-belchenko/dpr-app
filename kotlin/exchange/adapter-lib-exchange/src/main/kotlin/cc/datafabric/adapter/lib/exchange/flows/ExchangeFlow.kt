package cc.datafabric.adapter.lib.exchange.flows

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import org.bson.BsonDateTime
import org.bson.Document

class ExchangeFlow : ExchangeOperation() {
    var input: String? = null
    var output: String? = null
    lateinit var operationType: OperationType
    lateinit var whenMatched: WhenMatchedOperation
    lateinit var filterType: FilterType
    var pipeline: MutableList<Document>? = null
    var enabled: Boolean = true
    var stop: Boolean? = null // todo временно для отладки , убрать
    val fromTimestampPlaceholders:MutableList<BsonUtils.BsonProp> = mutableListOf()
    val toTimestampPlaceholders:MutableList<BsonUtils.BsonProp> = mutableListOf()
    var useDefaultFilter:Boolean? = null
    var mergeKey:List<String> = listOf( "_id")

    override fun getTriggerName():String?{
        return input
    }

//    var noun: String? = null
//    var verb: String? = null
//    var messageType: String? = null

    enum class OperationType {
        insert,  sync , replace , view , syncWithDelete, replaceWithDelete
    }
    enum class WhenMatchedOperation {
        replace,  merge
    }
    enum class FilterType {
        changedAt,  batchId, default , none
    }
    fun hasCustomTimestampFilter():Boolean{
        return  fromTimestampPlaceholders.any() || toTimestampPlaceholders.any()
    }
    fun applyTimestamps(lastTimestamp: BsonDateTime, newTimestamp: BsonDateTime){
        fromTimestampPlaceholders.forEach{
            it.document[it.name] = lastTimestamp
        }
        toTimestampPlaceholders.forEach{
            it.document[it.name] = newTimestamp
        }
    }
    companion object {

        fun fromDocument(document: Document): ExchangeFlow {
            val obj = ExchangeFlow()

            val operationType = (document["operationType"] ?: OperationType.insert).toString()
            val whenMatched = (document["whenMatched"] ?: WhenMatchedOperation.replace).toString()
            val filterType = (document["filterType"] ?: FilterType.default).toString()
            obj.input = document["input"].toString()
            obj.output = document["output"].toString()
            obj.src = document["src"]?.toString()
            obj.stop = document["stop"] as Boolean?
            obj.operationType = OperationType.valueOf(operationType)
            obj.whenMatched = WhenMatchedOperation.valueOf(whenMatched)
            obj.filterType = FilterType.valueOf(filterType)

            val mergeKey = document["mergeKey"]
            if (mergeKey!=null){

                if (mergeKey is String){
                    obj.mergeKey = listOf(mergeKey)
                }else{
                    obj.mergeKey = (mergeKey as List<*>).map { it as String }.toList()
                }

            }


            obj.pipeline = if (document["pipeline"] != null) {
                document["pipeline"] as MutableList<Document>
            } else {
                mutableListOf<Document>()
            }
            BsonUtils.getDocumentsRecursiveFromValue(obj.pipeline).forEach {objInfo->
                BsonUtils.getProperties(objInfo.document).forEach {propInfo->
                    if (propInfo.value=="$$\$FROM"){
                        obj.fromTimestampPlaceholders.add(propInfo)
                    }else if  (propInfo.value=="$$\$TO"){
                        obj.toTimestampPlaceholders.add(propInfo)
                    }
                }
            }
            obj.enabled = document["enabled"] as Boolean? ?: true
            obj.idSource = document["idSource"]?.toString()
            if (document["useDefaultFilter"]!=null){
                obj.useDefaultFilter = document["useDefaultFilter"] as Boolean
            }
            return obj
        }
    }
}
