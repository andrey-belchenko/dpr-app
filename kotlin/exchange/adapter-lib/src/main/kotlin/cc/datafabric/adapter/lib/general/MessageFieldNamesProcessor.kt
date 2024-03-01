package cc.datafabric.adapter.lib.general

import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.*
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
import org.bson.Document

object MessageFieldNamesProcessor {

    fun processIncoming(message: IncomingOutMessage) {
        val obj = message.asObject()
        Logger.traceFunBeg()
        obj.remove("_id")
        replaceInNames(obj, '_','.')

        Logger.traceFunEnd()
        Logger.traceObject(obj)
    }

    fun processOutgoing(message: OutgoingInMessage) {
        val obj = message.asObject()
        Logger.traceFunBeg()
        replaceInNames(obj, '.','_')
        Logger.traceFunEnd()
        Logger.traceObject(obj)
    }

    fun replaceInNames(document:Document, oldChar: Char, newChar: Char){
        BsonUtils.getDocumentsRecursive(document)
            .flatMap { d -> BsonUtils.getProperties(d.document) }
            .filter { it.name.contains(oldChar) }
            .toList().forEach {
                val doc = it.document
                doc[it.name.replace(oldChar, newChar)] = doc[it.name]
                doc.remove(it.name)
            }
    }

}