package cc.datafabric.adapter.config

import io.smallrye.config.ConfigMapping
import java.util.Optional


@ConfigMapping(prefix = "vocabulary")
interface VocabularyConfig {

    /**
     * Kafka topic to where notifications will be sent.
     */
    fun notificationTopic(): Optional<String>

    /**
     * If value is "kafka" KafkaNotifyingVocabularyServiceListener will be initialized.
     * Otherwise, NoOpVocabularyServiceListener listener will be used.
     */
    fun notificationMode(): Optional<String>

}