package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import org.bson.Document

object ExchangeProcessorModel {


    // todo закешировать пайплайны
    // todo  вынести пайплайнв в конфигурацию?


    fun processFinalModelEntitiesInput(
        batchId: String,
        operationId: String,
        keySource: String,
        flow: ExchangeFlow
    ) {

        val keyFieldName = ExchangeProcessor.getExternalKeyFieldName(keySource)
        val tempKeyFieldName = keyFieldName.replace(".", "_")
        IndexManager.create(Names.Collections.modelEntities, false, listOf(keyFieldName))
        val filter = Document()
        filter[Names.Fields.idSource] = keySource
        filter[Names.Fields.operationId] = operationId
        val itemsField = "items"
        val itemField = "item"

        val pipeline = listOf(
            Document("\$match", filter),
            // todo эта операция нужна потому что в одной порции могут попадаться полноценные записи, и урезанные (операция link)
            //есть фильтрация урезанных записей на этапе подготовки записей для вставки в sys_model_EntitiesInput
            //но она не всегда отрабатывает при больших объемах из за разбиения на более мелкие порции
            //данная группировка помогает, но похоже сильно замедляет выполнение пайплайна
            Document("\$group",
                    Document(mapOf(
                        "_id" to "\$id",
                        "item" to Document("\$mergeObjects","\$\$ROOT")
                    ))
                ),

            Document("\$replaceRoot", Document("newRoot", "\$item")),
//            Document("\$match", Document("\$expr","\$${Names.Fields.type}")),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.blockedMessages,
                        "localField" to Names.Fields.messageId,
                        "foreignField" to Names.Fields.id,
                        "as" to "blkMsg"
                    )
                )
            ),
            Document(
                "\$match", Document(
                    "\$expr", Document(
                        "\$eq",
                        listOf(Document("\$size", "\$blkMsg"), 0)
                    )
                )
            ),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelEntities,
                        "localField" to Names.Fields.id,
                        "foreignField" to keyFieldName,
                        "as" to itemsField
                    )
                )
            ),
            Document(
                "\$addFields",
                Document(
                    mapOf(
                        itemField to Document("\$arrayElemAt", listOf("\$$itemsField", 0))
                    )
                )
            ),

            Document(
                "\$project",
                Document(
                    mapOf(
                        Names.Fields.type to Document(
                            "\$ifNull",
                            listOf("\$$itemField.${Names.Fields.type}", "\$${Names.Fields.type}")
                        ),
                        Names.Fields.typeUpdated to Document(
                            "\$cond",
                            listOf(
                                Document(
                                    "\$and",
                                    listOf(
                                        "\$${Names.Fields.type}",
                                        "\$$itemField",
                                        Document( "\$not","\$$itemField.${Names.Fields.type}")
                                    )
                                ),
                                true,
                                "\$\$REMOVE"
                            )
                        ),
                        Names.Fields.initialId to Document(
                            "\$ifNull",
                            listOf("\$$itemField.${Names.Fields.initialId}", "\$${Names.Fields.uuid}")
                        ),
                        Names.Fields.id to "\$$itemField.${Names.Fields.id}",
                        Names.Fields.model to Document(
                            "\$switch",
                            Document(
                                "branches",
                                listOf(
                                    Document(
                                        mapOf(
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.create.toString()
                                                )
                                            ),
                                            //todo это сомнительно в том числе по производительности
                                            // наверное лучше перезаписывать объект если action=create но при этом нужно обработать старые поля model_Fields (связи?)
                                            // пока оставил merge
                                            "then" to Document(
                                                "\$mergeObjects",
                                                listOf(
                                                    "\$$itemField.${Names.Fields.model}",
                                                    "\$${Names.Fields.model}"
                                                )
                                            )
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.update.toString()
                                                )
                                            ),
                                            "then" to Document(
                                                "\$mergeObjects",
                                                listOf(
                                                    "\$$itemField.${Names.Fields.model}",
                                                    "\$${Names.Fields.model}"
                                                )
                                            )
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.delete.toString()
                                                )
                                            ),
                                            "then" to "\$$itemField.${Names.Fields.model}"
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            // EntityActionType.link означает что в нет полной информации по объекту, есть только ссылка не него
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.link.toString()
                                                )
                                            ),
                                            "then" to Document(
                                                "\$ifNull",
                                                listOf("\$$itemField.${Names.Fields.model}", "\$${Names.Fields.model}")
                                            )
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            // EntityActionType.link означает что в нет полной информации по объекту, есть только ссылка не него
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.deleteLink.toString()
                                                )
                                            ),
                                            "then" to Document(
                                                "\$ifNull",
                                                listOf("\$$itemField.${Names.Fields.model}", "\$${Names.Fields.model}")
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        Names.Fields.batchId to batchId,
                        Names.Fields.attr to Document(
                            "\$mergeObjects",
                            listOf("\$$itemField.${Names.Fields.attr}","\$${Names.Fields.attr}")
                        ),
                        Names.Fields.lastSource to Document(
                            "\$ifNull",
                            listOf("\$${Names.Fields.lastSource}", "\$$itemField.${Names.Fields.lastSource}")
                        ),
                        Names.Fields.messageId to Document(
                            "\$ifNull",
                            listOf("\$${Names.Fields.messageId}", "\$$itemField.${Names.Fields.messageId}")
                        ),
                        Names.Fields.operationId to operationId,
                        Names.Fields.createdAt to Document(
                            "\$switch",
                            Document(
                                mapOf(
                                    "branches" to listOf(
                                        Document(
                                            mapOf(
                                                "case" to  Document(
                                                    "\$in",
                                                    listOf(
                                                        "\$${Names.Fields.action}",
                                                        listOf(
                                                            ExchangeProcessor.EntityActionType.link.toString(),
                                                            ExchangeProcessor.EntityActionType.deleteLink.toString()
                                                        )
                                                    )
                                                ),
                                                "then" to  Document(
                                                    "\$ifNull",
                                                    listOf("\$$itemField.${Names.Fields.createdAt}", "\$\$NOW")
                                                )
                                            )
                                        ),
                                        Document(
                                            mapOf(
                                                "case" to "\$$itemField.${Names.Fields.deletedAt}",
                                                "then" to "\$\$NOW"
                                            )
                                        )

                                    ),
                                    "default" to Document(
                                        "\$ifNull",
                                        listOf("\$$itemField.${Names.Fields.createdAt}", "\$\$NOW")
                                    )
                                )
                            )
                        ),
                        Names.Fields.deletedAt to Document(
                            "\$switch",
                            Document(
                                mapOf(
                                    "branches" to listOf(
                                        Document(
                                            mapOf(
                                                "case" to Document(
                                                    "\$eq",
                                                    listOf(
                                                        "\$${Names.Fields.action}",
                                                        ExchangeProcessor.EntityActionType.delete.toString()
                                                    )
                                                ),
                                                "then" to "\$\$NOW"
                                            )
                                        ),
                                        Document(
                                            mapOf(
                                                "case" to Document(
                                                    "\$in",
                                                    listOf(
                                                        "\$${Names.Fields.action}",
                                                        listOf(
                                                            ExchangeProcessor.EntityActionType.link.toString(),
                                                            ExchangeProcessor.EntityActionType.deleteLink.toString()
                                                        )
                                                    )
                                                ),
                                                "then" to "\$$itemField.${Names.Fields.deletedAt}"
                                            )
                                        )
                                    ),
                                    "default" to "\$\$REMOVE"
                                )
                            )
                        ),

                        Names.Fields.changedAt to "\$\$NOW",
                        Names.Fields.extId to Document(
                            "\$ifNull",
                            listOf(
                                "\$${Names.Fields.extId}",
                                "\$$itemField.${Names.Fields.extId}"
                            ) // при импорте может приходить готовый extId
                        ),
                        tempKeyFieldName to "\$id",
                        Names.Fields.action to "\$${Names.Fields.action}"

                    )
                )
            ),
            Document(
                "\$addFields",
                Document(
                    mapOf(
                        keyFieldName to "\$$tempKeyFieldName",
                        "${Names.Fields.extId}.${Names.Values.platform}" to "\$${Names.Fields.initialId}",
//                                Document(
//                            "\$concat",
//                            listOf("\$${Names.Fields.type}", "_", "\$${Names.Fields.initialId}")
//                        ),
                        Names.Fields.id to Document(
                            "\$switch",
                            Document(
                                "branches",
                                listOf(
                                    Document(
                                        mapOf(
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.create.toString()
                                                )
                                            ),
                                            "then" to "\$${Names.Fields.initialId}"
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.update.toString()
                                                )
                                            ),
                                            "then" to "\$${Names.Fields.initialId}"
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            "case" to Document(
                                                "\$eq",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    ExchangeProcessor.EntityActionType.delete.toString()
                                                )
                                            ),
                                            "then" to Document(
                                                "\$concat",
                                                listOf("\$${Names.Fields.initialId}", "-deleted")
                                            )
                                        )
                                    ),
                                    Document(
                                        mapOf(
                                            // EntityActionType.link означает что в нет полной информации по объекту, есть только ссылка не него
                                            "case" to Document(
                                                "\$in",
                                                listOf(
                                                    "\$${Names.Fields.action}",
                                                    listOf(
                                                        ExchangeProcessor.EntityActionType.link.toString(),
                                                        ExchangeProcessor.EntityActionType.deleteLink.toString()
                                                    )
                                                )
                                            ),
                                            "then" to Document(
                                                "\$ifNull",
                                                listOf(
                                                    "\$$itemField.${Names.Fields.id}",
                                                    "\$${Names.Fields.initialId}"
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                    )
                )
            ),
            Document(
                "\$unset",
                listOf(
                    "_id",
                    tempKeyFieldName,
                    Names.Fields.action
                )
            ),
//            Document("\$match", Document("\$expr", "\$${Names.Fields.type}")),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelEntities,
                        "on" to Names.Fields.initialId,
                        "whenMatched" to "replace",
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntitiesInput,
            Names.Collections.modelEntities,
            pipeline,
            flow,
            batchId,
            operationId,
            "entities"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelEntities)
    }


    fun processFinalModelLinksInput(
        batchId: String,
        operationId: String,
        fromKeySource: String,
        toKeySource: String,
        flow: ExchangeFlow
    ) {

        val filter = Document()
        filter[Names.Fields.fromIdSource] = fromKeySource
        filter[Names.Fields.toIdSource] = toKeySource
        filter[Names.Fields.operationId] = operationId
        val fromKeyFieldName = ExchangeProcessor.getExternalKeyFieldName(fromKeySource)
        val toKeyFieldName = ExchangeProcessor.getExternalKeyFieldName(toKeySource)
        val nameFromItems = "fromItems"
        val nameFromItem = "fromItem"
        val nameToItems = "toItems"
        val nameToItem = "toItem"
        val pipeline = listOf(
            Document("\$match", filter),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.blockedMessages,
                        "localField" to Names.Fields.messageId,
                        "foreignField" to Names.Fields.id,
                        "as" to "blkMsg"
                    )
                )
            ),
            Document(
                "\$match", Document(
                    "\$expr", Document(
                        "\$eq",
                        listOf(Document("\$size", "\$blkMsg"), 0)
                    )
                )
            ),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelEntities,
                        "localField" to Names.Fields.fromId,
                        "foreignField" to fromKeyFieldName,
                        "as" to nameFromItems
                    )
                )
            ),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelEntities,
                        "localField" to Names.Fields.toId,
                        "foreignField" to toKeyFieldName,
                        "as" to nameToItems
                    )
                )
            ),
            Document(
                "\$addFields",
                Document(
                    mapOf(
                        nameFromItem to Document("\$arrayElemAt", listOf("\$$nameFromItems", 0)),
                        nameToItem to Document("\$arrayElemAt", listOf("\$$nameToItems", 0))
                    )
                )
            ),
            Document(
                "\$project",
                Document(
                    mapOf(
                        Names.Fields.fromId to "\$$nameFromItem.${Names.Fields.initialId}",
                        Names.Fields.predicate to "\$${Names.Fields.predicate}",
                        Names.Fields.inversePredicate to "\$${Names.Fields.inversePredicate}",
                        Names.Fields.fromType to "\$$nameFromItem.${Names.Fields.type}", //"\$${Names.Fields.fromType}",
                        Names.Fields.toType to "\$$nameToItem.${Names.Fields.type}",   //"\$${Names.Fields.toType}",
                        Names.Fields.toId to "\$$nameToItem.${Names.Fields.initialId}",
                        Names.Fields.isOneToOne to "\$${Names.Fields.isOneToOne}",

                        Names.Fields.linkId to Document(
                            "\$concat",
                            listOf("\$$nameFromItem.${Names.Fields.initialId}", "-", "\$${Names.Fields.predicate}")
                        ),
                        Names.Fields.batchId to batchId,
                        Names.Fields.attr to "\$${Names.Fields.attr}",
                        Names.Fields.lastSource to "\$${Names.Fields.lastSource}",
                        Names.Fields.messageId to "\$${Names.Fields.messageId}",
                        Names.Fields.operationId to operationId,
                        Names.Fields.changedAt to "\$\$NOW",
                        Names.Fields.deletedAt to Document(
                            "\$switch",
                            Document(
                                mapOf(
                                    "branches" to listOf(
                                        Document(
                                            mapOf(
                                                "case" to Document(
                                                    "\$eq",
                                                    listOf(
                                                        "\$${Names.Fields.action}",
                                                        ExchangeProcessor.EntityActionType.delete.toString()
                                                    )
                                                ),
                                                "then" to "\$\$NOW"
                                            )
                                        ),
                                        Document(
                                            mapOf(
                                                "case" to Document(
                                                    "\$eq",
                                                    listOf(
                                                        "\$${Names.Fields.action}",
                                                        ExchangeProcessor.EntityActionType.deleteLink.toString()
                                                    )
                                                ),
                                                "then" to "\$\$NOW"
                                            )
                                        )
                                    ),
                                    "default" to Document(
                                        "\$ifNull",
                                        listOf(
                                            "\$$nameFromItem.${Names.Fields.deletedAt}",
                                            "\$$nameToItem.${Names.Fields.deletedAt}",
                                            null
                                        )
                                    )
                                )
                            )
                        ),
                    )
                )
            ),
            Document(
                "\$addFields",
                Document(
                    mapOf(
                        Names.Fields.fullName to Document(
                            "\$concat",
                            listOf("\$${Names.Fields.fromType}", ".", "\$${Names.Fields.predicate}")
                        ),
                        Names.Fields.fullInverseName to Document(
                            "\$concat",
                            listOf("\$${Names.Fields.toType}", ".", "\$${Names.Fields.inversePredicate}")
                        ),
                    )
                )
            ),
            Document(
                "\$unset",
                listOf(
                    "_id"
                )
            ),
            //todo убрать
            Document("\$match", Document("\$expr", "\$${Names.Fields.fromId}")),
            Document("\$match", Document("\$expr", "\$${Names.Fields.toId}")),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelLinks,
                        "on" to listOf(
                            Names.Fields.fromId, Names.Fields.predicate
//                            , Names.Fields.toId
                        ),
                        //todo заменил "replace" на "merge" чтобы была возможность сохранить lastSource, но  сомнительно в плане логики и производительности
                        "whenMatched" to "merge",
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelLinksInput,
            Names.Collections.modelLinks,
            pipeline,
            flow,
            batchId,
            operationId,
            "links"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelLinks)
    }


    fun processFinalModelFieldsInput(
        batchId: String,
        operationId: String,
        keySource: String,
        flow: ExchangeFlow
    ) {
        val filter = Document()
        filter[Names.Fields.idSource] = keySource
        filter[Names.Fields.operationId] = operationId
        val keyFieldName = ExchangeProcessor.getExternalKeyFieldName(keySource)
        val nameItems = "items"
        val nameItem = "item"
        val pipeline = listOf(
            Document("\$match", filter),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.blockedMessages,
                        "localField" to Names.Fields.messageId,
                        "foreignField" to Names.Fields.id,
                        "as" to "blkMsg"
                    )
                )
            ),
            Document(
                "\$match", Document(
                    "\$expr", Document(
                        "\$eq",
                        listOf(Document("\$size", "\$blkMsg"), 0)
                    )
                )
            ),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelEntities,
                        "localField" to Names.Fields.id,
                        "foreignField" to keyFieldName,
                        "as" to nameItems
                    )
                )
            ),
            Document(
                "\$addFields",
                Document(
                    mapOf(
                        nameItem to Document("\$arrayElemAt", listOf("\$$nameItems", 0))
                    )
                )
            ),
            Document(
                "\$project",
                Document(
                    mapOf(
                        Names.Fields.id to "\$$nameItem.${Names.Fields.initialId}",
                        Names.Fields.type to "\$$nameItem.${Names.Fields.type}",
                        Names.Fields.predicate to "\$${Names.Fields.predicate}",
                        Names.Fields.value to "\$${Names.Fields.value}",
                        Names.Fields.fullName to "\$${Names.Fields.fullName}",
                        Names.Fields.fieldId to Document(
                            "\$concat",
                            listOf("\$$nameItem.${Names.Fields.initialId}", "-", "\$${Names.Fields.predicate}")
                        ),
                        Names.Fields.batchId to batchId,
                        Names.Fields.attr to "\$${Names.Fields.attr}",
                        Names.Fields.lastSource to "\$${Names.Fields.lastSource}",
                        Names.Fields.messageId to "\$${Names.Fields.messageId}",
                        Names.Fields.operationId to operationId,
                        Names.Fields.changedAt to "\$\$NOW"
                    )
                )
            ),
            Document(
                "\$unset",
                listOf(
                    "_id"
                )
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelFields,
                        "on" to listOf(
                            Names.Fields.id, Names.Fields.predicate
//                            , Names.Fields.toId
                        ),
                        //todo заменил "replace" на "merge" чтобы была возможность сохранить lastSource, но  сомнительно в плане логики и производительности
                        "whenMatched" to "merge",
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelFieldsInput,
            Names.Collections.modelFields,
            pipeline,
            flow,
            batchId,
            operationId,
            "fields"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelFields)
    }

    fun deleteRelatedLinks(batchId: String, operationId: String, flow: ExchangeFlow) {
        deleteRelatedLinks(batchId, operationId, flow, false)
        deleteRelatedLinks(batchId, operationId, flow, true)
    }

    private fun deleteRelatedLinks(
        batchId: String,
        operationId: String,
        flow: ExchangeFlow,
        isInverse: Boolean
    ) {
        val refId = if (isInverse) Names.Fields.fromId else Names.Fields.toId
        val pipeline = listOf(
            Document("\$match", Document(Names.Fields.operationId, operationId)),
            Document("\$match", Document(Names.Fields.deletedAt, Document("\$ne", null))),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelLinks,
                        "localField" to Names.Fields.initialId,
                        "foreignField" to refId,
                        "as" to "link"
                    )
                )
            ),
            Document("\$unwind", "\$link"),
            Document(
                "\$project",
                Document(
                    mapOf(
                        "_id" to false,
                        Names.Fields.fromId to "\$link.${Names.Fields.fromId}",
                        Names.Fields.predicate to "\$link.${Names.Fields.predicate}",
                        Names.Fields.deletedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.changedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.batchId to batchId,
                        Names.Fields.operationId to operationId,
                        Names.Fields.lastSource to "\$${Names.Fields.lastSource}",
                    )
                )
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelLinks,
                        "on" to listOf(Names.Fields.fromId, Names.Fields.predicate),
                        "whenMatched" to "merge"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntities,
            Names.Collections.modelLinks,
            pipeline,
            flow,
            batchId,
            operationId,
            "deleteRelatedLinks"
        )

        CollectionChangeInfoStore.setDummy(Names.Collections.modelLinks)
    }

    fun deleteRelatedFields(batchId: String, operationId: String, flow: ExchangeFlow) {

        val pipeline = listOf(
            Document("\$match", Document(Names.Fields.operationId, operationId)),
            Document("\$match", Document(Names.Fields.deletedAt, Document("\$ne", null))),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelFields,
                        "localField" to Names.Fields.initialId,
                        "foreignField" to Names.Fields.id,
                        "as" to "fld"
                    )
                )
            ),
            Document("\$unwind", "\$fld"),
            Document(
                "\$project",
                Document(
                    mapOf(
                        "_id" to false,
                        Names.Fields.id to "\$fld.${Names.Fields.id}",
                        Names.Fields.predicate to "\$fld.${Names.Fields.predicate}",
                        Names.Fields.deletedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.changedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.batchId to batchId,
                        Names.Fields.operationId to operationId,
                        Names.Fields.lastSource to "\$${Names.Fields.lastSource}",
                    )
                )
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelFields,
                        "on" to listOf(Names.Fields.id, Names.Fields.predicate),
                        "whenMatched" to "merge"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntities,
            Names.Collections.modelFields,
            pipeline,
            flow,
            batchId,
            operationId,
            "deleteRelatedFields"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelFields)
    }

    fun cascadeDelete(
        batchId: String,
        operationId: String,
        flow: ExchangeFlow
    ): ExchangeProcessor.ExecutionResult {
        val res1 = cascadeDelete(batchId, operationId, flow, true)
        val res2 = cascadeDelete(batchId, operationId, flow, false)
        res2.count += res1.count
        return res2
    }


    private fun cascadeDelete(
        batchId: String,
        operationId: String,
        flow: ExchangeFlow,
        isInverse: Boolean
    ): ExchangeProcessor.ExecutionResult {
        val fullNameProp = if (isInverse) Names.Fields.fullInverseName else Names.Fields.fullName
        val idProp = if (isInverse) Names.Fields.toId else Names.Fields.fromId
        val refIdProp = if (isInverse) Names.Fields.fromId else Names.Fields.toId
        val pipeline = listOf(
            Document("\$match", Document(Names.Fields.operationId, operationId)),
            Document("\$match", Document("\$expr", "\$" + Names.Fields.deletedAt)),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelLinks,
                        "let" to Document("id", "\$${Names.Fields.initialId}"),
                        "pipeline" to listOf(
                            Document(
                                "\$match",
                                Document("\$expr", Document("\$eq", listOf("\$$idProp", "\$\$id")))
                            ),
                            Document(
                                "\$match",
                                Document(
                                    "\$expr",
                                    Document(
                                        "\$in",
                                        listOf(
                                            "\$$fullNameProp",
                                            ExchangeSettingsRepository.cascadeDeleteLinks
                                        ) // todo можно разделить на прямые и обратные для оптимизации
                                    )
                                )
                            )
                        ),
                        "as" to "links"
                    )
                )
            ),
            Document("\$unwind", "\$links"),
//            Document("\$replaceRoot", Document("newRoot", "\$links")),
            //чтобы не обрабатывать удаленные повторно
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelEntities,
                        "localField" to "links.${refIdProp}",
                        "foreignField" to Names.Fields.id,
                        "as" to "entity"
                    )
                ),
            ),

            Document("\$unwind", "\$entity"),
            Document(
                "\$project",
                Document(
                    mapOf(
                        "_id" to false,
                        Names.Fields.initialId to "\$links.${refIdProp}",
                        Names.Fields.id to Document("\$concat", listOf("\$links.${refIdProp}", "-deleted")),
                        Names.Fields.deletedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.changedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.batchId to batchId,
                        Names.Fields.operationId to operationId,
                        Names.Fields.lastSource to "\$${Names.Fields.lastSource}",
                    )
                )
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelEntities,
                        "on" to Names.Fields.initialId,
                        "whenMatched" to "merge"
                    )
                )
            )
        )
         val result  = ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntities,
            Names.Collections.modelEntities,
            pipeline,
            flow,
            batchId,
            operationId,
            "cascadeDelete"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelEntities)
        return  result

    }




    fun updateLinkType(batchId: String, operationId: String, flow: ExchangeFlow){
        if (!checkNeedUpdateType(operationId)) return
        updateLinkType(batchId, operationId, flow, true)
        updateLinkType(batchId, operationId, flow, false)
    }

    private fun checkNeedUpdateType(operationId: String):Boolean{
        val filter = Document (
            mapOf(
                Names.Fields.operationId to operationId,
                Names.Fields.typeUpdated to true
            )
        )
        return ExchangeDatabase.getCollection(Names.Collections.modelEntities).find(filter).any()
    }

    private fun updateLinkType(batchId: String, operationId: String, flow: ExchangeFlow, isInverse: Boolean){
        val idField = if (isInverse) Names.Fields.fromId else Names.Fields.toId
        val typeField = if (isInverse) Names.Fields.fromType else Names.Fields.toType
        val pipeline = listOf(
            Document("\$match", Document(Names.Fields.operationId, operationId)),
            Document("\$match",Document(Names.Fields.typeUpdated, true)),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelLinks,
                        "localField" to Names.Fields.id,
                        "foreignField" to idField,
                        "as" to "l"
                    )
                )
            ),
            Document("\$unwind", "\$l"),
            Document(
                "\$project",
                Document(
                    mapOf(
                        "_id" to "\$l._id",
                        typeField to "\$${Names.Fields.type}",
                        Names.Fields.changedAt to "\$\$NOW",
                        Names.Fields.batchId to batchId,
                        Names.Fields.operationId to operationId,
                    )
                )
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelLinks,
                        "on" to "_id",
                        "whenMatched" to "merge"
                    )
                )
            )
        )

        ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntities,
            Names.Collections.modelLinks,
            pipeline,
            flow,
            batchId,
            operationId,
            "updateLinkEntityType"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelLinks)
    }

    fun updateRelatedEntitiesTimestamp(batchId: String, operationId: String, flow: ExchangeFlow) {
        updateRelatedEntitiesTimestamp(batchId, operationId, flow, false)
        updateRelatedEntitiesTimestamp(batchId, operationId, flow, true)
    }

    private fun updateRelatedEntitiesTimestamp(
        batchId: String,
        operationId: String,
        flow: ExchangeFlow,
        isInverse: Boolean
    ) {
        val refId = if (isInverse) Names.Fields.toId else Names.Fields.fromId
        val pipeline = listOf(
            Document("\$match", Document(Names.Fields.operationId, operationId)),
            Document("\$match", Document(Names.Fields.deletedAt, Document("\$ne", null))),
            // lookup чтобы не попадали удаленные
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.modelEntities,
                        "localField" to refId,
                        "foreignField" to Names.Fields.id,
                        "as" to "e"
                    )
                )
            ),
            Document("\$unwind", "\$e"),
            Document(
                "\$project",
                Document(
                    mapOf(
                        "_id" to false,
                        Names.Fields.initialId to "\$e.${Names.Fields.initialId}",
                        Names.Fields.changedAt to "\$${Names.Fields.deletedAt}",
                        Names.Fields.batchId to batchId,
                        Names.Fields.operationId to operationId,
//                        Names.Fields.lastSource to Names.Values.keep,
                    )
                )
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelEntities,
                        "on" to listOf(Names.Fields.initialId),
                        "whenMatched" to "merge"
                    )
                )
            )
        )

        ExchangeProcessor.executeAggregate(
            Names.Collections.modelLinks,
            Names.Collections.modelEntities,
            pipeline,
            flow,
            batchId,
            operationId,
            "updateRelatedEntitiesTimestamp"
        )
    }
}