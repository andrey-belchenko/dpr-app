package cc.datafabric.adapter.lib.general

import java.util.*

object MessageAccumulator {

    private var messages = mutableMapOf <String, IncomingOutMessage>()
    private const val batchSize = 100
    private const val timerInterval:Long = 1000

    private var messageProcessor: (Iterable<IncomingOutMessage>) -> Unit = {}

    fun setMessageProcessor(value:(Iterable<IncomingOutMessage>) -> Unit ) {
        messageProcessor = value
    }

    fun addMessage(message: IncomingOutMessage) {

        val entityId = message.mainEntityId()
        val oldMessage = messages[entityId]

        if (oldMessage!=null){
            if (oldMessage.getType()!=message.getType()){
                timer.cancel()
                processBatch()
            }
        }

        val isFirst = !messages.any()
        messages[entityId] = message
        if (messages.count() == batchSize) {
            timer.cancel()
            processBatch()
        } else if (isFirst) {
            startTimer()
        }
    }

    private fun processBatch(){
        messageProcessor(messages.values)
        messages = mutableMapOf()
    }


    private val timer = Timer()
    private fun startTimer() {
        val timerTask = object : TimerTask() {
            override fun run() {
                processBatch()
            }
        }
        timer.schedule(timerTask, timerInterval)
    }

}