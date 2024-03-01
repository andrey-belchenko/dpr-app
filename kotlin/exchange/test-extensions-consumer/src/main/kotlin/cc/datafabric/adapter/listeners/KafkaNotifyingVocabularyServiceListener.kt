package cc.datafabric.datalayer.http.listeners

import cc.datafabric.adapter.config.VocabularyConfig
import cc.datafabric.services.entities.VocabularyEntity
import cc.datafabric.services.listeners.VocabularyServiceListener
import cc.datafabric.transport.AppKafkaSender
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.arc.lookup.LookupIfProperty
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@LookupIfProperty(name = "vocabulary.notification-mode", stringValue = "kafka", lookupIfMissing = false)
class KafkaNotifyingVocabularyServiceListener(
    private val appKafkaSender: AppKafkaSender,
    private val vocabularyConfig: VocabularyConfig,
    private val objectMapper: ObjectMapper,
): VocabularyServiceListener {

    override fun onInsertion(vocabularyEntity: VocabularyEntity) {
        if (!vocabularyConfig.notificationTopic().isPresent) {
            return
        }

        val topic = vocabularyConfig.notificationTopic().get()
        val message = objectMapper.writeValueAsString(vocabularyEntity)
        appKafkaSender.syncSend(topic, null, message)
    }

}