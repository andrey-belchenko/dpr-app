package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import org.bson.Document

object ExchangeProcessorBlocked {


    // todo закешировать пайплайны
    // todo  вынести пайплайнв в конфигурацию?
    fun extendModelEntitiesInput(
        batchId: String,
        operationId: String,
        keySource: String,
        flow: ExchangeFlow
    ) {


        val filter = Document()
        filter[Names.Fields.idSource] = keySource
        filter[Names.Fields.operationId] = operationId

        val pipeline = listOf(
            Document("\$match", filter),
            Document(
                "\$addFields", Document(
                    mapOf(
                        Names.Fields.fullId to Document(
                            "\$concat", listOf(keySource, "-", "\$id")
                        )
                    )
                )
            ),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.forbiddenEntities,
                        "localField" to Names.Fields.fullId,
                        "foreignField" to  Names.Fields.fullId,
                        "as" to "blocked"
                    )
                )
            ),

            Document(
                "\$addFields", Document(
                    mapOf(
                        "blocked" to  Document(
                            "\$filter", Document(
                                mapOf(
                                    "input" to "\$blocked",
                                    "as" to "it",
                                    "cond" to Document("\$not","\$\$it.deletedAt"),
                                )
                            )
                        )
                    )
                )
            ),
            Document(
                "\$addFields", Document(
                    mapOf(

                        Names.Fields.isBlocked to Document(
                            "\$gt",
                            listOf(Document("\$size", "\$blocked"), 0)
                        ),
                        "blocked" to "\$\$REMOVE",
                    )
                )
            ),
            Document(
                "\$out", Names.Collections.modelEntitiesInputExt
            )

        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntitiesInput,
            Names.Collections.modelEntitiesInputExt,
            pipeline,
            flow,
            batchId,
            operationId,
            "entitiesInputExtend"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.modelEntitiesInputExt)
    }

    fun extractBlocked(
        batchId: String,
        operationId: String,
        keySource: String,
        flow: ExchangeFlow
    ) {
        // todo , разбить на части
        val filter = Document()
        filter[Names.Fields.idSource] = keySource
        filter[Names.Fields.operationId] = operationId


        val sysFields = Document(
            "\$addFields", Document(
                mapOf(
                    Names.Fields.id to "\$_id",
                    "_id" to "\$\$REMOVE",
                    Names.Fields.batchId to batchId,
                    Names.Fields.operationId to operationId,
                    Names.Fields.changedAt to "\$\$NOW",
                    Names.Fields.idSource to keySource,
                )
            )
        )

        var pipeline = listOf(
            Document("\$match", filter),
            Document("\$match", Document("\$expr", "\$${Names.Fields.isBlocked}")),
            Document("\$match", Document("\$expr", "\$${Names.Fields.messageId}")),
            Document(
                "\$group", Document(
                    mapOf(
                        "_id" to "\$${Names.Fields.messageId}",
                        "entities" to Document(
                            "\$push", Document(
                                mapOf(
                                    Names.Fields.id to "\$${Names.Fields.id}",
                                    Names.Fields.fullId to "\$${Names.Fields.fullId}",
                                    Names.Fields.idSource to keySource,
                                    Names.Fields.type to "\$${Names.Fields.type}",
                                    Names.Fields.name to "\$${Names.Fields.model}.IdentifiedObject_name",
                                )
                            )
                        )
                    )
                )
            ),
            Document(
                "\$lookup",
                Document(
                    mapOf(
                        "from" to Names.Collections.incomingMessages,
                        "localField" to "_id",
                        "foreignField" to Names.Fields.messageId,
                        "as" to "msg"
                    )
                )
            ),
            Document("\$unwind", "\$msg"),
            sysFields,
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.blockedMessages,
                        "on" to Names.Fields.id,
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.modelEntitiesInputExt,
            Names.Collections.blockedMessages,
            pipeline,
            flow,
            batchId,
            operationId,
            "blockedMessages"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.blockedMessages)



        pipeline = listOf(
            Document("\$match", filter),
            Document("\$sort",  Document("msg.${Names.Fields.changedAt}", -1)),
            Document(
                "\$group", Document(
                    mapOf(
                        "_id" to "\$msg.${Names.Fields.dtoId}",
                        Names.Fields.lastMessageId to Document(
                            "\$first", "\$_id"
                        ),
//                        Names.Fields.messageTimestamp  to Document(
//                            "\$first", "\$msg.${Names.Fields.changedAt}"
//                        ),
                        "entities" to Document(
                            "\$first", "\$entities"
                        )
                    )
                )
            ),
            Document("\$unwind", "\$entities"),
            Document(
                "\$project", Document(
                    mapOf(
                        "_id" to Document(
                            "\$concat", listOf("\$_id", ".", "\$entities.${Names.Fields.fullId}")
                        ),
                        Names.Fields.dtoId to "\$_id",
                        Names.Fields.lastMessageId to "\$${Names.Fields.lastMessageId}",
                        Names.Fields.entityId  to "\$entities.${Names.Fields.fullId}",
                        Names.Fields.entity  to "\$entities"
                    )
                )
            ),
            Document("\$unset", "entity.${Names.Fields.fullId}"),
            sysFields,
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.blockedDtoEntities,
                        "on" to Names.Fields.id,
                        "whenMatched" to "replace",
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )

        ExchangeProcessor.executeAggregate(
            Names.Collections.blockedMessages,
            Names.Collections.blockedDtoEntities,
            pipeline,
            flow,
            batchId,
            operationId,
            "blockedDto"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.blockedDtoEntities)


        pipeline = listOf(
            Document("\$match", filter),
            Document(
                "\$group", Document(
                    mapOf(
                        "_id" to "\$${Names.Fields.dtoId}",
                        Names.Fields.lastMessageId to Document(
                            "\$first", "\$${Names.Fields.lastMessageId}"
                        ),
                    )
                )
            ),
            sysFields,
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.blockedDto,
                        "on" to Names.Fields.id,
                        "whenMatched" to "replace",
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )

        ExchangeProcessor.executeAggregate(
            Names.Collections.blockedDtoEntities,
            Names.Collections.blockedDto,
            pipeline,
            flow,
            batchId,
            operationId,
            "blockedDto"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.blockedDto)

        pipeline = listOf(
            Document("\$match", filter),
            Document(
                "\$group", Document(
                    mapOf(
                        "_id" to "\$${Names.Fields.entityId}",
                        "item" to Document(
                            "\$first", "\$\$ROOT"
                        ),
                    )
                )
            ),
            Document(
                "\$addFields", Document(
                    mapOf(
                        "item.entity._id" to "\$_id",
                        "item.${Names.Fields.entityId}" to "\$\$REMOVE"
                    )
                )
            ),
            Document("\$replaceRoot", Document("newRoot", "\$item.entity")),
            sysFields,
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.blockedEntities,
                        "on" to Names.Fields.id,
                        "whenMatched" to "merge",
                        "whenNotMatched" to "insert"
                    )
                )
            )
        )
        ExchangeProcessor.executeAggregate(
            Names.Collections.blockedDtoEntities,
            Names.Collections.blockedEntities,
            pipeline,
            flow,
            batchId,
            operationId,
            "blockedEntities"
        )
        CollectionChangeInfoStore.setDummy(Names.Collections.blockedEntities)
    }


}