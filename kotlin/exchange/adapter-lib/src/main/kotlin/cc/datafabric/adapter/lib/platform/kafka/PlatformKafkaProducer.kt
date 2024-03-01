package cc.datafabric.adapter.lib.platform.kafka


import cc.datafabric.adapter.lib.general.IncomingOutMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import cc.datafabric.adapter.lib.sys.*
object PlatformKafkaProducer {

    private val producer = createProducer()
    private fun createProducer(): Producer<String, String> {
        val props = Properties()
        //todo вроде есть константы в библиотеке, заменить на константы
        props["bootstrap.servers"] = Config.get("adpKafkaBootstrapServer")
        props["key.serializer"] = StringSerializer::class.java.canonicalName
        props["value.serializer"] = StringSerializer::class.java.canonicalName
        //props["retries"] = 0 //todo не работает, все равно продолжает пытаться отправлять при ошибке
        return KafkaProducer(props)
    }
    fun send(message: IncomingOutMessage) {
        Logger.traceFunBeg()
        val text = message.getText()
        val futureResult = producer.send(
            ProducerRecord(Config.get("adpKafkaTopic"), message.getId(),text)
        )
        futureResult.get()
        Logger.traceFunEnd()
    }

}