package cc.datafabric.adapter.lib.exchange.flows

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import org.bson.Document
import java.util.UUID

class ExchangeRule private constructor() : ExchangeMultiStepOperation(){
    var trigger: String? = null

//    val id = UUID.randomUUID()
    lateinit var tags:Set<String>
    var name:String?=null

    override fun getOperations(document: Document):Iterable<Document>{
        if (document["operation"] ==  null) {
            return listOf(document)
        }
        return super.getOperations(document)
    }

    override fun getTriggerName():String?{
        return trigger
    }
    companion object {
        fun fromDocument(document: Document): ExchangeRule {
            val obj = ExchangeRule()
            obj.initOperationProps(document)
            val trigger = document["trigger"] ?: document["input"] ?: obj.getTriggerName()
            ?: throw Exception("can't determine trigger for ${BsonUtils.toJsonString(document)}")
            obj.trigger = trigger.toString()
            obj.tags = if (document["tags"]!=null) {
                (document["tags"] as List<String>).toSet()
            }else{
                setOf()
            }
            obj.name = document["name"]?.toString()
            return obj
        }
    }
}
