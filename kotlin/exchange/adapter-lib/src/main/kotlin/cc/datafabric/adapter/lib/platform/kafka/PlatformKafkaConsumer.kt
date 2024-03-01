package cc.datafabric.adapter.lib.platform.kafka


import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.time.Duration
import java.util.*
import cc.datafabric.adapter.lib.sys.*
object PlatformKafkaConsumer {
    fun consume(messageProcessor: (String) -> Unit){

        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG ] = Config.get("adpKafkaBootstrapServer")
        props[ConsumerConfig.GROUP_ID_CONFIG] = Config.get("adpKafkaGroupId")
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.canonicalName
        //todo какие доп параметры нужны?
        val consumer = KafkaConsumer<String, String>(props)
        consumer.subscribe(listOf(Config.get("adpKafkaTopic")))
        Logger.traceBeg("kafka consuming")
        while (true) {
            //todo какой таймаут поставить?
            val records: ConsumerRecords<String, String> = consumer.poll(Duration.ofMillis(100))
            for (record in records) {
                messageProcessor(record.value())
            }
        }
    }
}