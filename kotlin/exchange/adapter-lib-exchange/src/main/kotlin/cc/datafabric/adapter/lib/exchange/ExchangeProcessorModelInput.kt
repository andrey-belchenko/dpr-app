package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow

import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document


object ExchangeProcessorModelInput {


    class ModelInputResult {
        val affectedEntityIdSources = hashSetOf<String>()
        val affectedLinkIdSources = hashSetOf<Pair<String, String>>()
        var hasDeleted = false
        var hasPlatformMessages =
            false // todo это костыль, чтобы отправлять дополнительно сообщение в платформу через kafka
    }


    fun processRowModelInsert(batchId: String, operationId: String, flow: ExchangeFlow): ModelInputResult {

        val filter = Document(Names.Fields.operationId, operationId)
        val docs = ExchangeDatabase.db.getCollection(Names.Collections.modelInput).find(filter)
        val combinedResult = rowModelInsert(batchId, operationId, flow, docs)
        return combinedResult
    }

    fun rowModelInsert(
        batchId: String,
        operationId: String,
        flow: ExchangeFlow,
        docs: Iterable<Document>
    ): ModelInputResult {
        val portionDocs = mutableListOf<Document>()
        val combinedResult = ModelInputResult()
        val portionMaxSize = 10000
        fun processPortion() {
            val result = processRowModelInsertPortion(portionDocs, batchId, operationId, flow)
            if (result.hasDeleted) {
                combinedResult.hasDeleted = true
            }
            if (result.hasPlatformMessages) {
                combinedResult.hasPlatformMessages = true
            }
            result.affectedEntityIdSources.forEach {
                if (!combinedResult.affectedEntityIdSources.contains(it)) {
                    combinedResult.affectedEntityIdSources.add(it)
                }
            }
            result.affectedLinkIdSources.forEach {
                if (!combinedResult.affectedLinkIdSources.contains(it)) {
                    combinedResult.affectedLinkIdSources.add(it)
                }
            }
            portionDocs.clear()
        }

        docs.forEach {
            if (portionMaxSize == portionDocs.count()) {
                processPortion()
            }
            portionDocs.add(it)
        }
        processPortion()
        return combinedResult
    }

    private fun readDocuments(documents: Iterable<Document>): List<Document> {
        return Logger.traceFun {
            return@traceFun documents.toList()
        }
    }

    private fun processRowModelInsertPortion(
        documents: Iterable<Document>,
        batchId: String,
        operationId: String,
        flow: ExchangeFlow
    ): ModelInputResult {
        return Logger.traceFun {
            val result = ModelInputResult()

            val entityList = mutableMapOf<String, Document>()
            val linkList = mutableMapOf<String, Document>()
            val propsList = mutableMapOf<String, Document>()

            val docs = readDocuments(documents)

            docs.forEach { rootDoc ->
                try {
                    processRowModelDocument(rootDoc, result, batchId, operationId, entityList, linkList, propsList)
                } catch (e: Exception) {
                    throw Exception("${e.message} \n ${BsonUtils.toJsonString(rootDoc)}", e)
                }
            }
            IndexManager.createDefined()
            insertPortion(entityList, flow, batchId, operationId, propsList, linkList)
            return@traceFun result
        }
    }

    private fun setDocField(document: Document, fieldName: String, value: Any?) {
        if (value != null) {
            document[fieldName] = value

        } else {
            document.remove(fieldName)
        }
    }

    private fun setMessageId(document: Document, value: Any?) {
        setDocField(document, Names.Fields.messageId, value)
    }

    private fun processRowModelDocument(
        rootDoc: Document,
        result: ModelInputResult,
        batchId: String,
        operationId: String,
        entityList: MutableMap<String, Document>,
        linkList: MutableMap<String, Document>,
        propsList: MutableMap<String, Document>
    ) {
        if (rootDoc[Names.Fields.verb] != null) {
            result.hasPlatformMessages = true
        }
        val skipObjFields = listOf(Names.Fields.atId, Names.Fields.atAttr, Names.Fields.atLastSource)
        val messageId = rootDoc[Names.Fields.messageId]?.toString()
        BsonUtils.getDocumentsRecursiveFromValue(rootDoc[Names.Fields.model])
            .filter { it.parentProp == null || !skipObjFields.contains(it.parentProp!!.name) }
            .forEach { objInfo ->

                val obj = objInfo.document
                val id = obj[Names.Fields.atId]
                val type = obj[Names.Fields.atType]
                if (type != null) {
                    ExchangeProfile.ensureExists(type.toString())
                }
                if (id == null) {
                    throw Exception("Id for item $type not found")
                }

                fun getIdSourceForObject(obj: Document): String {
                    val objId = obj[Names.Fields.atId]
                    var value = (obj[Names.Fields.atIdSource]?.toString()
                        ?: rootDoc[Names.Fields.idSource]?.toString())
                    if (value == null) {
                        val action = obj[Names.Fields.atAction]?.toString()
                        value = if (
                            action == null ||
                            action == ExchangeProcessor.EntityActionType.delete.toString() ||
                            objId is Document // при записи в modelImport id может быть в формате {"КИСУР": "VS010-0000074", "platform": "a863b974-6f8b-407d-83e7-a4969f36de23"}
                        ) {
                            Names.Values.platform
                        } else {
                            Names.Values.processor
                        }
                    }
                    return value
                }

                val idSrc = getIdSourceForObject(obj)

                val entityDoc = prepareRawModelEntity(objInfo.document, rootDoc, idSrc)
                setMessageId(entityDoc, messageId)

                entityDoc[Names.Fields.idSource] = idSrc
                entityDoc[Names.Fields.batchId] = batchId
                entityDoc[Names.Fields.operationId] = operationId
//                setDocField(entityDoc,Names.Fields.isBlocked,objInfo.document[Names.Fields.atIsBlocked])
                if (!result.affectedEntityIdSources.contains(idSrc)) {
                    result.affectedEntityIdSources.add(idSrc)
                }
                val entId = entityDoc[Names.Fields.id].toString()

                if (entityDoc[Names.Fields.action]?.toString() == ExchangeProcessor.EntityActionType.delete.toString()) {
                    result.hasDeleted = true
                }

                val linkActions = listOf(
                    ExchangeProcessor.EntityActionType.link,
                    ExchangeProcessor.EntityActionType.deleteLink
                )
                val action = ExchangeProcessor.EntityActionType.valueOf(entityDoc[Names.Fields.action].toString())

                if (entityDoc[Names.Fields.type] == null) {
                    entityDoc.remove(Names.Fields.type)
                }

                if (entityList[entId] == null || !linkActions.contains(action)) {
                    val prev = entityList[entId]
                    if (prev is Document) {
                        if (
                            prev[Names.Fields.idSource] == entityDoc[Names.Fields.idSource] &&
                            prev[Names.Fields.type] != null &&
                            entityDoc[Names.Fields.type] != null &&
                            prev[Names.Fields.type] != entityDoc[Names.Fields.type]
                        ) {
                            throw Exception(
                                "same id $entId was used for entities with different types ${prev[Names.Fields.type]} and ${entityDoc[Names.Fields.type]}"
                            )
                        }
                        // todo мерджим при create, правильно ли это?
                        // todo и не проверено
                        if (action != ExchangeProcessor.EntityActionType.delete && prev[Names.Fields.model] != null) {
                            val prevModel = prev[Names.Fields.model] as Document
                            val model = entityDoc[Names.Fields.model] as Document
                            BsonUtils.getProperties(model).forEach {
                                prevModel[it.name] = it.value
                            }
                            entityDoc[Names.Fields.model] = prevModel
                        }

                    }

                    entityList[entId] = entityDoc
                }



                if (objInfo.parentProp != null) {
                    val parentIdSrc = getIdSourceForObject(objInfo.parentProp!!.document)
                    //todo условие пока не проверено, расчет на то что,
                    // эти связи будут удалены массово после удаления объектов с помощью deleteRelatedLinks,
                    // а для случая, когда в наборе операций пересоздаются объекты delete потом create, связи не будут затронуты
                    if (objInfo.document[Names.Fields.atAction] != ExchangeProcessor.EntityActionType.delete.toString()) {
                        val linkDoc =
                            prepareRawModelLink(objInfo.parentProp!!, objInfo.document, rootDoc, parentIdSrc, idSrc)
                        if (linkDoc != null) { // todo !!!!!!!!!!!!!!!!!!!!! временно для загрузки
                            setMessageId(linkDoc, messageId)
                            linkDoc[Names.Fields.batchId] = batchId
                            linkDoc[Names.Fields.operationId] = operationId

                            if (linkDoc[Names.Fields.fromType] != null) {
                                val dmName = "${Names.Prefixes.dataMart}_${linkDoc[Names.Fields.fromType].toString()}"
                                IndexManager.define(
                                    dmName,
                                    false,
                                    listOf(Names.Fields.model + "." + linkDoc[Names.Fields.predicate].toString())
                                )
                            }

                            val linkId = "${linkDoc[Names.Fields.fromId]}-${linkDoc[Names.Fields.predicate]}"
                            //                        linkDoc[Names.Fields.linkId] = linkId
                            linkList[linkId] = linkDoc
                            val idSourcePair = Pair(
                                linkDoc[Names.Fields.fromIdSource].toString(),
                                linkDoc[Names.Fields.toIdSource].toString()
                            )
                            if (!result.affectedLinkIdSources.contains(idSourcePair)) {
                                result.affectedLinkIdSources.add(idSourcePair)
                            }
                        }
                    }
                }


                if (listOf(
                        ExchangeProcessor.EntityActionType.create,
                        ExchangeProcessor.EntityActionType.update
                    ).contains(action)
                ) {
                    BsonUtils.getProperties(entityDoc[Names.Fields.model] as Document).forEach {
                        val propDoc = prepareRawModelProperty(it.name, it.value, objInfo.document, rootDoc, idSrc)
                        setMessageId(propDoc, messageId)
                        propDoc[Names.Fields.batchId] = batchId
                        propDoc[Names.Fields.operationId] = operationId
                        val propId = "${propDoc[Names.Fields.id]}-${propDoc[Names.Fields.predicate]}"
                        //                            propDoc[Names.Fields.fieldId] = propId
                        propsList[propId] = propDoc
                    }
                }
            }
    }

    private fun insertPortion(
        entityList: MutableMap<String, Document>,
        flow: ExchangeFlow,
        batchId: String,
        operationId: String,
        propsList: MutableMap<String, Document>,
        linkList: MutableMap<String, Document>
    ) {
        Logger.traceFun {
            if (entityList.any()) {
                val items = entityList.values.toList()
                ExchangeDatabase.db.getCollection(Names.Collections.modelEntitiesInput).insertMany(items)
                ExchangePipelineLogger.log(
                    Names.Collections.modelInput,
                    Names.Collections.modelEntitiesInput,
                    null,
                    flow.src,
                    items.count().toLong(),
                    batchId,
                    operationId,
                    "entitiesInput",
                    null
                )
            }

            if (propsList.any()) {
                val items = propsList.values.toList()
                ExchangeDatabase.db.getCollection(Names.Collections.modelFieldsInput).insertMany(items)
                ExchangePipelineLogger.log(
                    Names.Collections.modelInput,
                    Names.Collections.modelFieldsInput,
                    null,
                    flow.src,
                    items.count().toLong(),
                    batchId,
                    operationId,
                    "fieldsInput",
                    null
                )
            }

            if (linkList.any()) {
                val items = linkList.values.toList()
                ExchangeDatabase.db.getCollection(Names.Collections.modelLinksInput).insertMany(items)
                ExchangePipelineLogger.log(
                    Names.Collections.modelInput,
                    Names.Collections.modelLinksInput,
                    null,
                    flow.src,
                    items.count().toLong(),
                    batchId,
                    operationId,
                    "linksInput",
                    null
                )
            }
        }

    }


    private fun applyLastSourceValue(obj: Document, attr: Any?) {
        if (attr == null) {
            obj[Names.Fields.lastSource] = Names.Values.processor
        } else {
            if (attr != Names.Values.keep) {
                obj[Names.Fields.lastSource] = attr
            }
        }
    }

    private fun prepareRawModelEntity(obj: Document, root: Document, idSource: String): Document {
        val entity = Document()
        val model = Document()

        val action =
            if (obj[Names.Fields.atAction] != null) {
                ExchangeProcessor.EntityActionType.valueOf(obj[Names.Fields.atAction].toString())
            } else {
                ExchangeProcessor.EntityActionType.link
            }
        if (listOf(
                ExchangeProcessor.EntityActionType.create,
                ExchangeProcessor.EntityActionType.update
            ).contains(action)
        ) {
            entity[Names.Fields.model] = model
            val skipFields = Names.Fields.sysFields
            obj.forEach {
                if (
                    !(it.value is Document || it.value is List<*>)
                    && !skipFields.contains(it.key)
                ) {
                    model[it.key] = it.value
                }
            }
        }

        val inId = obj[Names.Fields.atId]

        entity[Names.Fields.id] = getObjectId(obj)
        if (inId is Document) {
            entity[Names.Fields.extId] = inId
            entity[Names.Fields.uuid] = inId[Names.Values.platform]
        } else if (idSource == Names.Values.platform) {
            entity[Names.Fields.uuid] = inId
        } else {
            entity[Names.Fields.uuid] = ExchangeUuid.get(entity[Names.Fields.id].toString())
        }
        entity[Names.Fields.type] = obj[Names.Fields.atType]
        entity[Names.Fields.action] = action.toString()
        entity[Names.Fields.changedAt] = root[Names.Fields.changedAt]
        entity[Names.Fields.attr] = obj[Names.Fields.atAttr]
        applyLastSourceValue(entity, obj[Names.Fields.atLastSource])

        return entity
    }

    private fun getObjectId(obj: Document): String {
        val inId = obj[Names.Fields.atId]
        val id = if (inId is Document) {
            inId[Names.Values.platform].toString()
        } else {
            inId.toString()
        }
        return id
    }


    private fun prepareRawModelLink(
        parentProp: BsonUtils.BsonProp,
        obj: Document,
        root: Document,
        parentIdSource: String,
        childIdSource: String
    ): Document? {


        val type = parentProp.document[Names.Fields.atType].toString()

        //todo вычислить заранее в профиле

        val propSourceType = parentProp.name.split("_").first()
        val isMultiple = ExchangeProfile.isMultiple(propSourceType, parentProp.name)
        val inverseName = ExchangeProfile.getInverseName(propSourceType, parentProp.name)
        //todo вычислить заранее в профиле
        val inversePropSourceType = inverseName?.split("_")?.first()
        val predicate: String?
        val inversePredicate: String?
        val fromType: String?
        val toType: String?
        val fromId: String?
        val toId: String?
        val fromIdSource: String?
        val toIdSource: String?
        val parentObj = parentProp.document
        var toPropSourceType: String? = null
        var isOneToOne = false
        val objId = getObjectId(obj)
        val parentObjId = getObjectId(parentObj)
        if (!isMultiple) {
            predicate = parentProp.name
            inversePredicate = inverseName
            fromType = parentObj[Names.Fields.atType] as String?
            toType = obj[Names.Fields.atType] as String?
            fromId = parentObjId
            toId = objId
            fromIdSource = parentIdSource
            toIdSource = childIdSource
            toPropSourceType = inversePropSourceType
        } else {
            predicate = inverseName
            inversePredicate = parentProp.name
            fromType = obj[Names.Fields.atType] as String?
            toType = parentObj[Names.Fields.atType] as String?
            fromId = objId
            toId = parentObjId
            fromIdSource = childIdSource
            toIdSource = parentIdSource
            toPropSourceType = propSourceType
        }
        if (inversePredicate != null && !ExchangeProfile.isMultiple(toPropSourceType!!, inversePredicate)) {
            isOneToOne = true
        }


//        fun errorContext():String{
//            return "\n ${
//                BsonUtils.toJsonString(
//                    root
//                )
//            }"
//        }

        if (!isMultiple && parentProp.isArray) {
            // todo убираю эту проверку чтобы обработалась связь многие ко многим в поле IdentifiedObject_OrganisationRoles
            //  например для id @id:fab3fa29-2fc7-4c93-a13f-6a79dc361131 воронеж
            //  ожидаемый эффект в модель запишется последнее значение
            //  доработать возможность хранить связи многие ко многим
            // throw Exception("Property ${parentProp.name} of object ${Names.Fields.atId}:${parentProp.document[Names.Fields.atId]} does not support multiple values")
        }

        if (isMultiple && inverseName == null) {
            throw Exception("Can't find inverse predicate for property ${parentProp.name} of object ${Names.Fields.atId}:${parentProp.document[Names.Fields.atId]}")
        }


        val link = Document()
        link[Names.Fields.fromId] = fromId
        link[Names.Fields.toId] = toId
        link[Names.Fields.action] = obj[Names.Fields.atAction]
        link[Names.Fields.changedAt] = root[Names.Fields.changedAt]
        link[Names.Fields.predicate] = predicate
        link[Names.Fields.inversePredicate] = inversePredicate
        link[Names.Fields.fromType] = fromType
        link[Names.Fields.toType] = toType
        link[Names.Fields.fromIdSource] = fromIdSource
        link[Names.Fields.toIdSource] = toIdSource
        link[Names.Fields.isOneToOne] = isOneToOne
        link[Names.Fields.attr] = obj[Names.Fields.atAttr]
        applyLastSourceValue(link, obj[Names.Fields.atLastSource])
//        link[Names.Fields.fullName] = "${link[Names.Fields.fromType]}.${link[Names.Fields.predicate]}"
//        link[Names.Fields.fullInverseName] = "${link[Names.Fields.toType]}.${link[Names.Fields.inversePredicate]}"
        return link
    }

    private fun prepareRawModelProperty(
        predicate: String,
        value: Any?,
        obj: Document,
        root: Document,
        idSource: String
    ): Document {


        val id = getObjectId(obj)
        if (obj[Names.Fields.atType] == null) {
            throw Exception("Type not found for item $id \n {${BsonUtils.toJsonString(obj)}}")
        }
        val type = obj[Names.Fields.atType] as String
//        if (predicate.contains("normallyInService")){
//            val x =1
//        }
        ExchangeProfile.ensureExists(type, predicate)

        val property = Document()
        property[Names.Fields.id] = id
        property[Names.Fields.type] = type
        property[Names.Fields.changedAt] = root[Names.Fields.changedAt]
        property[Names.Fields.predicate] = predicate
        property[Names.Fields.value] = value
        property[Names.Fields.idSource] = idSource
        property[Names.Fields.fullName] = "$type.$predicate"
        property[Names.Fields.attr] = obj[Names.Fields.atAttr]
        applyLastSourceValue(property, obj[Names.Fields.atLastSource])
        return property
    }

}