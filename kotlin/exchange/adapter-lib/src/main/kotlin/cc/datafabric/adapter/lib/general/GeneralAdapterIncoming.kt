package cc.datafabric.adapter.lib.general
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.tools.MessageLogger


object GeneralAdapterIncoming {


    fun processMessage(message: String): Iterable<IncomingOutMessage> {
        return Logger.traceFun {
            Logger.traceData(message)

            val inMessage = IncomingInMessage(message)
            val eventName = inMessage.getEventName()
            Logger.traceData("event name: '$eventName'")
            MessageLogger.log(message, "in", mapOf("type" to eventName))
            val pipelineResult = PipelineProcessor.exec(message = inMessage, pipelineName = eventName)
            pipelineResult.forEach { outMsg ->
                val info = mapOf("type" to outMsg.getType())
                MessageLogger.log(outMsg, "out-raw", info)
                MessageFieldNamesProcessor.processIncoming(outMsg)
                IncomingMessageKeysProcessor.process(outMsg)
                outMsg.generateId()
                MessageLogger.log(outMsg, "out", info)
                //todo доделать
//            Stopwatch.start("customProcessor")
//            val customProcessor = CustomMessageProcessor.fromExtension("adapter-extension-example")
//            customProcessor.process(outMsg.getText())
//            Stopwatch.stop("customProcessor")
            }
            return@traceFun pipelineResult
        }
    }
}