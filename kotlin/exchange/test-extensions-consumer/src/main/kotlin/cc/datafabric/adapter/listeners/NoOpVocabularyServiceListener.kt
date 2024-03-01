package cc.datafabric.datalayer.http.listeners

import cc.datafabric.services.entities.VocabularyEntity
import cc.datafabric.services.listeners.VocabularyServiceListener
import io.quarkus.arc.lookup.LookupIfProperty
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
@LookupIfProperty(name = "vocabulary.notification-mode", stringValue = "kafka", lookupIfMissing = true)
class NoOpVocabularyServiceListener: VocabularyServiceListener {

    override fun onInsertion(vocabularyEntity: VocabularyEntity) {}

}