package cc.datafabric.adapter.lib.general


import cc.datafabric.adapter.lib.common.*
import java.util.*
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
object OutgoingMessageKeysProcessor {
    fun process(message: OutgoingOutMessage) {
        Logger.traceFunBeg()
        message.getEntities().toList().forEach {
            val value = it.getKey()
            if (value!=null){
                it.setKey(value.split(':').last())
            }
            Logger.traceObject(message)
        }
        Logger.traceFunEnd()
    }
}