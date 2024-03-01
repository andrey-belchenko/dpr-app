package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import cc.datafabric.adapter.lib.exchange.flows.ExchangeMultiStepOperation
import cc.datafabric.adapter.lib.exchange.flows.ExchangeOperation
import cc.datafabric.adapter.lib.exchange.flows.ExchangeRule
import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.MongoCommandException
import org.bson.BsonDateTime
import org.bson.Document
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis


object ExchangeProcessor {


    fun action(changedCollections: Iterable<ExchangeListener.ChangedCollection>) {
        Logger.traceFun {

            ExchangeSettingsRepository.loadIfNeed()
            if (changedCollections.any()) {
                processChanges(changedCollections)
            }
        }
    }

    private var batchCounter: Int = 0 // чтобы удобнее было отслеживать в логе визуально

    class OperationExecutionResult(
        val modelAffected: Boolean,
        val messageInputAffected: Boolean,
    )

//    fun runRules (
//        rules: Iterable<ExchangeRule>,
//        filterType: ExchangeFlow.FilterType
//    ): OperationExecutionResult {
//        val operationCalls = mutableListOf<OperationCall>()
//        rules.forEach { rule->
//            val changedFilter =  Document()
//            val source = ExchangeListener.ChangedCollection(
//                rule.getTriggerName()!!,
//                changedFilter,
//                BsonUtils.getMinTimeStamp(),
//                BsonUtils.getMaxTimeStamp(),
//            )
//            val operationCall = OperationCall(source, rule)
//            operationCalls.add(operationCall)
//        }
//       return  processOperations(null,null,operationCalls,true)
//    }

//    fun runRules (
//        rules: Iterable<ExchangeRule>,
//        onlyChanges:Boolean
//    ): OperationExecutionResult {
//        val operationCalls = mutableListOf<OperationCall>()
//        rules.forEach { rule->
//            val changedFilter =  Document()
//            val source = ExchangeListener.ChangedCollection(
//                rule.getTriggerName()!!,
//                changedFilter,
//                BsonUtils.getMinTimeStamp(),
//                BsonUtils.getMaxTimeStamp(),
//            )
//            val operationCall = OperationCall(source, rule)
//            operationCalls.add(operationCall)
//        }
//        return  processOperations(null,null,operationCalls,true)
//    }
//
//    fun runRulesForChanges (
//        rules: Iterable<ExchangeRule>
//    ): OperationExecutionResult {
//        val rulesBySource = rules.groupBy { it.getTriggerName()!!}.toMap()
//        rulesBySource.forEach { source, rule ->
//            ExchangeListener.getChanges(listOf(source)){res->
//                res.changedCollections.forEach {
//
//                }
//            }
//        }
//        val sources =  mutableMapOf<String,ExchangeListener.ChangedCollection>()
//        val operationCalls = mutableListOf<OperationCall>()
//        rules.forEach { rule->
//            val changedFilter =  Document()
//            val source = ExchangeListener.ChangedCollection(
//                rule.getTriggerName()!!,
//                changedFilter,
//                BsonUtils.getMinTimeStamp(),
//                BsonUtils.getMaxTimeStamp(),
//            )
//            val operationCall = OperationCall(source, rule)
//            operationCalls.add(operationCall)
//        }
//        return  processOperations(null,null,operationCalls,true)
//    }


    private fun processOperations(
        parentBatchId: String?,
        parentOperationId: String?,
        operations: Iterable<OperationCall>,
        isParallel: Boolean
    ): OperationExecutionResult {
        val batchId = parentBatchId ?: getNextButchId()
        if (isParallel) {
            return processParallelOperations(batchId, parentOperationId, operations)
        } else {
            return processSequentialOperations(batchId, parentOperationId, operations)
        }
    }


    private fun processParallelOperations(
        batchId: String,
        parentOperationId: String?,
        operations: Iterable<OperationCall>
    ): OperationExecutionResult {

        val operationId = parentOperationId ?: UUID.randomUUID().toString()


        // была ConcurrentModificationException пытался решить таким образом, но проблема была те тут, все равно оставил
        val modelAffectedCounter = AtomicInteger()
        val messageInputAffectedCounter = AtomicInteger()

        operations.toList().parallelStream().forEach { opCall ->
            val result = processOperation(batchId, operationId, opCall.collection, opCall.operation)
            if (result.modelAffected) {
                modelAffectedCounter.incrementAndGet()
            }
            if (result.messageInputAffected) {
                messageInputAffectedCounter.incrementAndGet()
            }
        }
        val modelAffected = modelAffectedCounter.get()>0
        val messageInputAffected = messageInputAffectedCounter.get()>0
        return OperationExecutionResult(modelAffected, messageInputAffected)
    }

    private fun processSequentialOperations(
        batchId: String,
        parentOperationId: String?,
        operations: Iterable<OperationCall>
    ): OperationExecutionResult {

        var modelAffected = false
        var messageInputAffected = false
        val lastIndex = operations.count() - 1
        var index = 0
        operations.forEach { opCall ->
            var operationId = UUID.randomUUID().toString()
            val isLast = lastIndex == index
            if (isLast) {
                operationId = parentOperationId ?: operationId
            }
            val result = processOperation(batchId, operationId, opCall.collection, opCall.operation)
            if (!isLast || parentOperationId == null) {
                if (result.modelAffected) {
                    processModelInput(batchId, operationId)
                }
                if (result.messageInputAffected) {
                    processMessageInput(batchId, operationId)
                }
            } else {
                modelAffected = result.modelAffected
                messageInputAffected = result.messageInputAffected
            }
            index++
        }
        return OperationExecutionResult(modelAffected, messageInputAffected)
    }


    private fun processOperation(
        batchId: String,
        operationId: String,
        source: ExchangeListener.ChangedCollection,
        operation: ExchangeOperation
    ): OperationExecutionResult {
        if (operation is ExchangeFlow) {
            processFlow(batchId, operationId, source, operation)
            return OperationExecutionResult(
                operation.output == Names.Collections.modelInput,
                operation.output == Names.Collections.messageInput
            )
        } else if (operation is ExchangeMultiStepOperation) {
            return processRule(batchId, operationId, source, operation)
        }
        throw NotImplementedError()
    }

    private fun processRule(
        batchId: String,
        operationId: String,
        source: ExchangeListener.ChangedCollection,
        rule: ExchangeMultiStepOperation
    ): OperationExecutionResult {
        val calls = rule.operation.map { OperationCall(source, it) }
        return processOperations(batchId, operationId, calls, rule.isParallel)
    }


    private class OperationCall(
        val collection: ExchangeListener.ChangedCollection,
        val operation: ExchangeOperation,
//        val isSingleOperation:Boolean
    )


    private fun processChanges(changedCollections: Iterable<ExchangeListener.ChangedCollection>) {
        Logger.traceFun {
//            var commands:Iterable<Document>? =  null
            val operationCalls = mutableListOf<OperationCall>()
            val flowStateTags = ExchangeStatusStore.getFlowStatesTags()
            changedCollections.forEach { source ->
//                if (source.name==Names.Collections.processorCommand){
//                    commands = source.getDocuments()
//                }
                ExchangeSettingsRepository.getBySource(source.name).forEach { rule ->
                    if (
                        !flowStateTags.disabledFlowsTags.intersect(rule.tags).any() ||
                        flowStateTags.enabledFlowsTags.intersect(rule.tags).any()
                    ) {
                        val operationCall = OperationCall(source, rule)
                        operationCalls.add(operationCall)
                    }
                }
            }
            var operationId = UUID.randomUUID().toString()
            val batchId = getNextButchId()
            val result = processOperations(batchId, operationId, operationCalls, true)
            if (result.modelAffected) {
                processModelInput(batchId, operationId)
            }
            if (result.messageInputAffected) {
                processMessageInput(batchId, operationId)
            }
            CollectionChangeInfoStore.commit()
//            if (commands!=null){
//                CommandsHandler.handle(commands!!)
//            }
        }
    }


    private fun getNextButchId(): String {
        batchCounter++
        if (batchCounter >= 100) {
            batchCounter = 1
        }
        return batchCounter.toString().padStart(2, '0') + " " + UUID.randomUUID().toString()
    }


    private fun processFlow(
        batchId: String,
        operationId: String,
        source: ExchangeListener.ChangedCollection?,
        flow: ExchangeFlow,
    ) {
        var useFilter = flow.useDefaultFilter
        if (flow.hasCustomTimestampFilter()){
            //todo
            // если есть пользовательский фильтр по датам, системный фильтр исключается
            // при заполнении коллекции out_Sk11 эти фильтры друг другу мешали
            // не уверен что это корректно во всех случаях
            useFilter = false
            if (flow.useDefaultFilter!=false){
                flow.applyTimestamps(source!!.lastTimestamp!!, source!!.newTimestamp!!)
            }else{
                flow.applyTimestamps(BsonUtils.getMinTimeStamp(),BsonUtils.getMaxTimeStamp())
            }
        }
        if (useFilter==null) {
            useFilter = true
        }
        var filterType = ExchangeFlow.FilterType.none
        if (useFilter){
            filterType = flow.filterType
            if (filterType==ExchangeFlow.FilterType.default){
                filterType = if (source != null && source.name == flow.input){
                    ExchangeFlow.FilterType.changedAt
                } else {
                    ExchangeFlow.FilterType.batchId
                }
            }
        }

        val filter = when(filterType){
            ExchangeFlow.FilterType.changedAt->source!!.filter
            ExchangeFlow.FilterType.batchId->Document(Names.Fields.batchId, batchId)
            ExchangeFlow.FilterType.none->Document()
            else -> throw NotImplementedError()
        }

        ExchangeProcessorPipeline.processFlow(batchId, operationId, flow, filter)

    }

    // для использования в расширении платформы
    fun modelInput(docs: Iterable<Document>) {
        val operationId = UUID.randomUUID().toString()
        val batchId = getNextButchId()
        val flow = ExchangeFlow()
        val result = ExchangeProcessorModelInput.rowModelInsert(batchId, operationId, flow, docs)
        processModelInput(result, batchId, operationId, flow)
    }

    private fun processMessageInput(
        batchId: String, operationId: String,
    ) {

        ExchangeProcessorMessageInput.processMessageInput(batchId, operationId)

    }

    private fun processModelInput(
        batchId: String, operationId: String,
        //todo не понятно что используется из flow, разобраться заменить на интерфейс
        // похоже только flow.src
//        flow:ExchangeFlowDefinition
    ) {
        val flow = ExchangeFlow() // todo костыль
        val result = ExchangeProcessorModelInput.processRowModelInsert(batchId, operationId, flow)
        processModelInput(result, batchId, operationId, flow)
    }

    private fun processModelInput(
        result: ExchangeProcessorModelInput.ModelInputResult,
        batchId: String,
        operationId: String,
        flow: ExchangeFlow
    ) {
        result.affectedEntityIdSources.forEach {
            ExchangeProcessorBlocked.extendModelEntitiesInput(batchId, operationId, it, flow)
            ExchangeProcessorBlocked.extractBlocked(batchId, operationId, it, flow)
            ExchangeProcessorModel.processFinalModelEntitiesInput(batchId, operationId, it, flow)
            ExchangeProcessorModel.processFinalModelFieldsInput(batchId, operationId, it, flow)
        }

        result.affectedLinkIdSources.forEach {
            ExchangeProcessorModel.processFinalModelLinksInput(batchId, operationId, it.first, it.second, flow)
        }

        if (result.affectedLinkIdSources.any()){
            ExchangeProcessorModel.updateLinkType(batchId, operationId, flow)
        }

        if (result.hasDeleted) {
            var hasDeleted = true
            while (hasDeleted) {
                // todo проверен только 1 уровень
                hasDeleted = ExchangeProcessorModel.cascadeDelete(batchId, operationId, flow).count > 0
                if (hasDeleted) {
                    ExchangeProcessorModel.deleteRelatedFields(batchId, operationId, flow)
                    ExchangeProcessorModel.deleteRelatedLinks(batchId, operationId, flow)
                }
            }
            ExchangeProcessorModel.deleteRelatedFields(batchId, operationId, flow)
            ExchangeProcessorModel.deleteRelatedLinks(batchId, operationId, flow)

            ExchangeProcessorModel.updateRelatedEntitiesTimestamp(batchId, operationId, flow)
        }

        if (result.hasPlatformMessages) {
            // todo отказался от этой функциональности, проверить, убрать ExchangeProcessorPlatformInput и все что с этим связано
            ExchangeProcessorPlatformInput.processPlatformInput(batchId, operationId, flow)
        }
        ExchangeProcessorDataMart.processDataMartEntities(batchId, operationId, flow)
    }

    fun getExternalKeyFieldName(keySource: String): String {
        return "${Names.Fields.extId}.${keySource}"
    }


    class ExecutionResult(var count: Long)

    fun executeAggregate(
        source: String,
        target: String,
        pipeline: List<Document>,
        flow: ExchangeFlow,
        batchId: String,
        operationId: String,
        sysAction: String?
    ): ExecutionResult {

        return Logger.traceFun(source, target) {
            val executionId = UUID.randomUUID().toString()
            try {
//                if (flow.input=="dm_LineSpan" && flow.output=="view_aplInfo"){
//                    val x = "1"
//                }
                val extPipeline = mutableListOf<Document>()
                val stepsCount = pipeline.count()
                var curStep = 0
                pipeline.forEach {
                    if (curStep == stepsCount - 1) {
                        extPipeline.add(
                            Document(
                                "\$addFields",
                                Document(
                                    mapOf(
                                        Names.Fields.executionId to executionId
                                    )
                                )
                            )
                        )
                        // todo костыль чтобы системных полей не было в витринах при просмотре через препаратор (для демонстрации)
                        if (target.startsWith("view.")) {
                            extPipeline.add(
                                Document(
                                    "\$unset",
                                    listOf(Names.Fields.batchId, Names.Fields.executionId, Names.Fields.operationId)
                                )
                            )
                        }
                    }
                    extPipeline.add(it)
                    curStep++
                }

                val elapsed = measureTimeMillis {
                    ExchangeDatabase.getCollection(source).aggregate(extPipeline).allowDiskUse(true).toCollection()
                }

                // todo производительность
                IndexManager.create(target, false, listOf(Names.Fields.executionId))

                val count = ExchangeDatabase.getCollection(target)
                    .countDocuments(Document(Names.Fields.executionId, executionId))
                ExchangePipelineLogger.log(
                    source,
                    target,
                    pipeline,
                    flow.src,
                    count,
                    batchId,
                    operationId,
                    sysAction,
                    elapsed
                )
                if (count > 0) {
                    CollectionChangeInfoStore.set(target)
                }
                return@traceFun ExecutionResult(count)
            } catch (e: MongoCommandException) {
                Logger.traceData(ExchangePipelineLogger.getPipelineText(source, pipeline))
                val errorInfo = Document()
                    .append("message", e.message)
                    .append("stackTrace", e.stackTraceToString())
                ExchangePipelineLogger.log(
                    source,
                    target,
                    pipeline,
                    flow.src,
                    0,
                    batchId,
                    operationId,
                    sysAction,
                    null,
                    errorInfo
                )
                throw e

            }

        }
    }

    enum class EntityActionType {
        create, delete, link, update, deleteLink
    }
}