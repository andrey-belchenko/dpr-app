package cc.datafabric.adapter.lib.common.tools

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.common.XmlElement
import cc.datafabric.adapter.lib.general.IncomingOutMessage
import cc.datafabric.adapter.lib.general.OutgoingOutMessage
import cc.datafabric.adapter.lib.sys.*
import org.apache.jena.rdf.model.Resource
import org.bson.BsonDateTime


import org.bson.Document

object LoggerExtension{
    private var targetVersionId: Int? = null
    fun Logger.traceObject(value: Document?) {
        if (Config.isTraceDataDisabled()) return
        if (value == null){
            traceData(null)
            return
        }
        traceData(BsonUtils.toJsonString(value))
    }

    fun Logger.traceObject(value: Iterable<Document>) {
        if (Config.isTraceDataDisabled()) return
        value.forEach {
            traceData(BsonUtils.toJsonString(it))
        }
    }

    fun Logger.traceObject(value: XmlElement) {
        if (Config.isTraceDataDisabled()) return
        traceData(value.toXmlString())
    }

    fun Logger.traceObject(value: IncomingOutMessage) {
        if (Config.isTraceDataDisabled()) return
        traceData(value.getText())
    }

    fun Logger.traceObject(value: OutgoingOutMessage) {
        if (Config.isTraceDataDisabled()) return
        traceData(value.getText())
    }

    fun Logger.traceObject(value: Resource?) {
        if (Config.isTraceDataDisabled()) return
        traceData(value.toString())
    }
    fun Logger.traceObject(value: Counter) {
        if (Config.isTraceDataDisabled()) return
        traceData(value.toString())
    }

    fun Logger.traceObject(value: BsonDateTime) {
        if (Config.isTraceDataDisabled()) return
        traceData(value.toString())
    }
}



