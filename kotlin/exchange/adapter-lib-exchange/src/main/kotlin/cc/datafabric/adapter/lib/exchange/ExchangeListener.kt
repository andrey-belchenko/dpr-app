package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.BsonDateTime
import org.bson.Document
import java.util.*
import kotlin.system.exitProcess

object ExchangeListener {
    private val timerInterval: Long by lazy {
        Config.get(ConfigNames.exchangeAgentIntervalMs).toLong()
    }
    private val timer = Timer()
    fun listenCollections(collectionNames: Iterable<String>, handler: (ListenerResult) -> Unit) {
        try {
            ExchangeStatusStore.setProcessorStatusWaiting()
        } catch (ex: Exception) {
            handleException(ex)
        }
        Logger.traceFun {
            startTimer(handler, collectionNames)
        }
    }

    fun listen(handler: (ListenerResult) -> Unit) {
        try {
            ExchangeStatusStore.setProcessorStatusWaiting()
        } catch (ex: Exception) {
            handleException(ex)
        }
        Logger.traceFun {
            startTimer(handler, null)
        }
    }

    private fun startTimer(handler: (ListenerResult) -> Unit, collectionNames: Iterable<String>?) {
        Logger.traceFun {
            val timerTask = object : TimerTask() {
                override fun run() {
                    try {
                        Logger.timerActionStarted()
                        if (!ExchangeStatusStore.getProcessorDisabled()) {
                            var result = true
                            var iteration = 1
                            while (result) {
                                result = action(handler, collectionNames, iteration)
                                iteration++
                            }
                        }
                        startTimer(handler, collectionNames)
                        Logger.timerActionFinished()
                    } catch (ex: Exception) {
                        handleException(ex)

                    }
                }
            }
            timer.schedule(timerTask, timerInterval)
        }
    }

    private fun buildChangedCollectionsResult(changedCollections: Iterable<CollectionChangeInfoStore.CollectionChangeInfo>): Iterable<ChangedCollection> {
        val changedCollectionsResult = mutableListOf<ChangedCollection>()
        changedCollections.forEach { col ->
            val lastCollectionTimestamp = ProcessorTimestamp.get(col.name)
            val changedFilter = prepareChangedFilter(lastCollectionTimestamp, col.changedAt)
            changedCollectionsResult.add(
                ChangedCollection(
                    col.name, changedFilter, lastCollectionTimestamp, col.changedAt
                )
            )
        }
        return changedCollectionsResult
    }

    fun getChanges(collectionNames: Iterable<String>? = null, handler: (ListenerResult) -> Unit) {
        val lastTimestamp = ProcessorTimestamp.get()
        val newTimestamp = ExchangeTimestamp.now()
        val changedCollections = CollectionChangeInfoStore.getChangedFrom(lastTimestamp)
            .filter { collectionNames == null || collectionNames.contains(it.name) }.toList()
        val changedCollectionsResult = buildChangedCollectionsResult(changedCollections)
        if (changedCollections.any()) {
            handler(ListenerResult(changedCollectionsResult, lastTimestamp, newTimestamp))
            changedCollections.parallelStream().forEach { col ->
                ProcessorTimestamp.set(col.name, col.changedAt)
            }
        }
        ProcessorTimestamp.set(newTimestamp)
    }

    private fun action(
        handler: (ListenerResult) -> Unit, collectionNames: Iterable<String>?, iteration: Int = 1
    ): Boolean {

        ExchangeUuid.readMode()
        // todo это косвенный признак что БД не проинициализирована, придумать вариант получше
        if (!ExchangePipelineLogger.any()) {
            IndexManager.reset()
            ViewManager.createViews()
        }

        val lastTimestamp = ProcessorTimestamp.get()
        val newTimestamp = ExchangeTimestamp.now()
        var changedCollections = CollectionChangeInfoStore.getChangedFrom(lastTimestamp).toList()

        if (collectionNames != null) {
            changedCollections = changedCollections.filter { collectionNames.contains(it.name) }
        }
        if (changedCollections.any()) {
            if (!ExchangeStatusStore.isProcessorStatusProcessing()) {
                ExchangeStatusStore.setProcessorStatusProcessing()
                ExchangeUuid.loadIdsIfNeed()

            }
            Logger.workStarted()
            val changedCollectionsResult = buildChangedCollectionsResult(changedCollections)
            handler(ListenerResult(changedCollectionsResult, lastTimestamp, newTimestamp))
            changedCollections.parallelStream().forEach { col ->
                ProcessorTimestamp.set(col.name, col.changedAt)
            }
            ExchangeStatusStore.setProcessorCompletionTime()
            Logger.workFinished()
        } else {
            if (!ExchangeStatusStore.isProcessorStatusWaiting()) {
                ExchangeUuid.saveIdsIfNeed()
                ExchangeStatusStore.setProcessorStatusWaiting()

            }
        }
        ProcessorTimestamp.set(newTimestamp)
        return changedCollections.any()
    }

    private fun handleException(exception: Exception) {
        try {
            ExchangeErrorLogger.log(exception.message, exception.stackTraceToString())
            ExchangeStatusStore.setProcessorStatusError()
            ExchangeStatusStore.setProcessorErrorTime()
        } catch (_: Exception) {
            exit(exception)
        }
        exit(exception)
    }

    private fun prepareChangedFilter(lastTimestamp: BsonDateTime, newTimestamp: BsonDateTime): Document {
        return Document(
            "\$and", listOf(
                Document(Names.Fields.changedAt, Document("\$gte", lastTimestamp)),
                Document(Names.Fields.changedAt, Document("\$lt", newTimestamp))
            )
        )
    }

    private fun exit(exception: Exception) {
        Logger.status("error")
        exception.printStackTrace()
        Logger.traceBeg("exit")
        exitProcess(1)
    }


    class ListenerResult(
        val changedCollections: Iterable<ChangedCollection>,
        val lastTimestamp: BsonDateTime,
        val newTimestamp: BsonDateTime
    )

    class ChangedCollection(
        val name: String,
        val filter: Document,
        val lastTimestamp: BsonDateTime? = null,
        val newTimestamp: BsonDateTime? = null
    ) {


        fun getDocuments(): Iterable<Document> {
            return ExchangeDatabase.db.getCollection(name).find(filter).sort(Document(Names.Fields.changedAt, 1))
        }
    }

}