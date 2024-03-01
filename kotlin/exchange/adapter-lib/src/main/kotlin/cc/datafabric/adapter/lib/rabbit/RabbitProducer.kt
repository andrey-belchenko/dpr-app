package cc.datafabric.adapter.lib.rabbit


import cc.datafabric.adapter.lib.sys.*
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import cc.datafabric.adapter.lib.common.ConfigNames
object RabbitProducer {


    private var channel: Channel? =null
    private val configQueueName  by lazy {
        val queue = Config.get(ConfigNames.rabbitQueue)
        bindQueue(queue)
        queue
    }
    private val exchange = Config.get(ConfigNames.rabbitExchange)

    private  val boundQueues = mutableSetOf<String>()

    private  fun bindQueue(queueName:String){
        if (boundQueues.contains(queueName)) return
        if (exchange.isNotBlank()) {
            channel!!.queueBind(queueName, exchange, "")
        }
        boundQueues.add(queueName)
    }
    init {
        Logger.traceFun {
            val factory = ConnectionFactory()
            factory.setUri(Config.get(ConfigNames.rabbitUri))
            val connection = factory.newConnection()
            channel = connection.createChannel()
//            channel?.queueDeclare(queueName, false, false, false, null);
        }
    }

    fun produce(message:String) {
        channel!!.basicPublish(exchange, configQueueName, null, message.toByteArray());
    }

    fun produce(queueName:String, message:String) {
        bindQueue(queueName)
        channel!!.basicPublish(exchange, queueName, null, message.toByteArray());
        Logger.traceData("queueName:${queueName}")
        Logger.traceData("message:${message}")
    }


}