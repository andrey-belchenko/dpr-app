package cc.datafabric.adapter.lib.rabbit


import cc.datafabric.adapter.lib.sys.*
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.system.exitProcess
import cc.datafabric.adapter.lib.common.ConfigNames
object RabbitConsumer {


    fun consume(messageHandler: (String) -> Unit) {
        initChannel()
        consuming(messageHandler)
    }

    private var channel: Channel? =null
    private var queueNames:Iterable< String>?=null
    private fun initChannel() {
        Logger.traceFun {
            val factory = ConnectionFactory()
            factory.setUri(Config.get(ConfigNames.rabbitUri))
            //todo Ограничено для целей отладки
            val es = Executors.newFixedThreadPool(1)
            val connection = factory.newConnection(es)
            channel = connection.createChannel()
            val exchange = Config.get(ConfigNames.rabbitExchange)
            queueNames = Config.getArray(ConfigNames.rabbitQueue)
            if (exchange.isNotBlank()) {
                queueNames!!.forEach {
                    Logger.status("listening queue $it")
                    channel!!.queueBind(it, exchange, "")
                }
            }
        }
    }

    private fun consuming(messageHandler: (String) -> Unit){
        Logger.traceFunBeg()
        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            try {
                consumeMessage(delivery,messageHandler)
            } catch (ex: Exception) {
                Logger.status("error")
                //todo пока возвращаем сообщение в очередь и завершаем процесс. подумать как правильно обрабатывать сбои
                // предполагается, что в этом случае оркестратор перезапускает контейнер
                basicNack(delivery)
                ex.printStackTrace()
                Logger.traceBeg("exit")
                exitProcess(1)
            }
        }
        queueNames!!.forEach {
            channel!!.basicConsume(it, false, deliverCallback) { _: String? -> }
        }

    }

    private fun consumeMessage(delivery: Delivery, messageHandler: (String) -> Unit){
        Logger.traceFun {
            val message = String(delivery.body, StandardCharsets.UTF_8)
            messageHandler(message)
            basicAck(delivery)
        }
    }

    private fun basicAck(delivery: Delivery){
        Logger.traceFun {
            channel!!.basicAck(delivery.envelope.deliveryTag, false)
        }
    }

    private fun basicNack(delivery: Delivery){
        Logger.traceFun {
            channel!!.basicNack(delivery.envelope.deliveryTag, false, true)
        }
    }


}