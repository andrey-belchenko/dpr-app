package cc.datafabric.adapter.lib.exchange

object Names {
    object Collections {
        const val modelEntitiesInput = "sys_model_EntitiesInput"
        const val modelEntitiesInputExt = "sys_model_EntitiesInputExt"
        const val modelLinksInput = "sys_model_LinksInput"
        const val modelFieldsInput = "sys_model_FieldsInput"


        const val forbiddenEntities = "sys_model_ForbiddenEntities"
        const val blockedEntities = "sys_model_BlockedEntities"
        const val blockedDto = "sys_model_BlockedDto"
        const val blockedDtoEntities = "sys_model_BlockedDtoEntities"
        const val incomingMessages = "sys_IncomingMessages"
        const val blockedMessages = "sys_model_BlockedMessages"
        const val messageInput = "sys_MessageInput"

        // todo логика работы с этими коллекциями размазана между кодом процессора и TS конфигурацией (правила), требуется рефакторинг, все в правилах или все тут?
        const val unblockedEntities = "sys_model_UnblockedEntities"
        const val unblockedDto = "sys_model_UnblockedDto"
        const val extraIdMatching = "sys_model_ExtraIdMatching"
        const val unblockedDtoEntities = "sys_model_UnblockedDtoEntities"


        const val modelEntities = "model_Entities"
        const val modelLinks = "model_Links"
        const val modelFields = "model_Fields"
        const val modelInput = "sys_model_Input"
        const val settings = "sys_Settings"
        const val pythonProjects = "sys_pythonProjects"
        const val modelImport = "sys_model_Import"
        const val platformInput = "sys_platform_Input"
        const val dummy = "sys_Dummy"
        const val messageLog = "sys_MessageLog"
        const val pipelineLog = "sys_PipelineLog"
        const val collectionChangeInfo = "sys_CollectionChangeInfo"
        const val processorCollectionTimestamp = "sys_ProcessorCollectionTimestamp"
        const val processorTimestamp = "sys_ProcessorTimestamp"
//        const val processorCommand = "sys_ProcessorCommand"
//        const val processorCompletedCommand = "sys_ProcessorCompletedCommand"
        const val  scheduledActions = "sys_scheduledActions"
        const val  asyncWebOperations = "sys_asyncWebOperations"

    }

    object Fields {
        private const val atPrefix = "@"
        const val fileId = "fileId"
        const val createdAt = "createdAt"
        const val changedAt = "changedAt"
        const val deletedAt = "deletedAt"
        const val payload = "payload"
        const val model = "model"
        const val dtoId = "dtoId"
        const val eventId = "eventId"
        const val messageId = "messageId"
        const val objectId = "objectId"
        const val lastMessageId = "lastMessageId"
        const val entityId = "entityId"
        const val entity = "entity"

        //        const val messageTimestamp = "messageTimestamp"
        const val isBlocked = "isBlocked"
        const val deletedModel = "deletedModel"
        const val action = "action"
        const val id = "id"
        const val fullId = "fullId"
        const val platformId = "platformId"
        const val initialId = "initialId"
        const val fromId = "fromId"
        const val toId = "toId"
        const val idSource = "idSource"

        const val attr = "attr"
        const val lastSource = "lastSource"
        const val fromIdSource = "fromIdSource"
        const val toIdSource = "toIdSource"
        const val isOneToOne = "isOneToOne"
        const val type = "type"
        const val typeUpdated = "typeUpdated"
        const val name = "name"
        const val atAction = "$atPrefix$action"
        const val atId = "$atPrefix$id"
        const val atIdSource = "$atPrefix$idSource"
        const val atAttr = "$atPrefix$attr"
        const val atLastSource = "$atPrefix$lastSource"
        const val atType = "$atPrefix$type"
        const val uuid = "uuid"
        const val extId = "extId"
        const val predicate = "predicate"
        const val value = "value"
        const val inversePredicate = "inversePredicate"
        const val fromType = "fromType"
        const val toType = "toType"

        //        const val isSingle = "isSingle"
//        const val isSystem = "isSystem"
        const val batchId = "batchId"
        const val executionId = "executionId"
        const val operationId = "operationId"
        const val operations = "operations"
        const val linkId = "linkId"
        const val fieldId = "fieldId"
        const val fullName = "fullName"
        const val fullInverseName = "fullInverseName"
        const val sysAction = "sysAction"

        //        const val order  = "_order"
        val sysFields = listOf(atAction, atId, atIdSource, atType, atAttr, atLastSource)
        const val noun = "noun"
        const val verb = "verb"
        const val messageType = "type"
        const val processorName = "processorName"
        const val collectionName = "collectionName"
        const val service = "service"
        const val processorStatus = "processorStatus"
        const val processorDisabled = "processorDisabled"
        const val onlyLogging = "onlyLogging"
        const val processorProgress = "processorProgress"
        const val completedTimestamp = "completedTimestamp"
        const val errorTimestamp = "errorTimestamp"
        const val timestamp = "timestamp"
        const val filterName = "filterName"
        const val uuidMode = "uuidMode"
        const val isSent = "isSent"
        const val sendingId = "sendingId"
    }

    object Prefixes {
        const val income = "in"
        const val dataMart = "dm"
    }

    object Values {
        const val platform = "platform"
        const val processor = "processor"
        const val listening = "listening"
        const val processing = "processing"
        const val error = "error"
        const val keep = "keep"
        const val uiUrlVar = "[uiUrl]"
    }
}