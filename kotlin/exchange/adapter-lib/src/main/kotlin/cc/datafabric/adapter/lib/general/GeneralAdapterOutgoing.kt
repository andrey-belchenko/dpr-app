package cc.datafabric.adapter.lib.general
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.tools.MessageLogger


object GeneralAdapterOutgoing {


    fun processMessage(message: String): Iterable<OutgoingOutMessage> {
        return Logger.traceFun {
            Logger.traceData(message)
            val inMessage = OutgoingInMessage(message)
            val type = inMessage.getType() ?: throw  Exception("can't find message type")
            Logger.traceData("type: '$type'")
            MessageLogger.log(message, "in", mapOf("type" to type))
            MessageFieldNamesProcessor.processOutgoing(inMessage)
            MessageLogger.log(inMessage, "in-processed", mapOf("type" to type))
            val pipelineResult = PipelineProcessor.exec(message = inMessage, pipelineName = type)
            pipelineResult.forEach { outMsg ->
                val info = mapOf("type" to outMsg.getEventName())
                MessageLogger.log(outMsg, "out-raw", info)
                OutgoingMessageKeysProcessor.process(outMsg)
                MessageLogger.log(outMsg, "out", info)
            }
            return@traceFun listOf()
        }
    }
}