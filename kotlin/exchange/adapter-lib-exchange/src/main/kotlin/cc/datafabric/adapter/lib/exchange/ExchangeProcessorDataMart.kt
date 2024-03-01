package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import org.bson.BsonNull
import org.bson.Document



object ExchangeProcessorDataMart {



    fun processDataMartEntities(batchId:String,operationId:String, flow: ExchangeFlow) {
       val filter = Document(Names.Fields.operationId, operationId)
        val pipeline = listOf(
            Document("\$match", filter),
            Document("\$match", Document("\$expr","\$${Names.Fields.type}")),
            Document()
                .append(
                    "\$project", Document()
                        .append(Names.Fields.type, "\$${Names.Fields.type}")
                        .append("_id", 0)
                ),
            Document()
                .append(
                    "\$group", Document()
                        .append("_id", BsonNull())
                        .append(
                            "distinct", Document()
                                .append("\$addToSet", "$\$ROOT")
                        )
                ),
            Document()
                .append(
                    "\$unwind", Document()
                        .append("path", "\$distinct")
                        .append("preserveNullAndEmptyArrays", false)
                ),
            Document()
                .append(
                    "\$replaceRoot", Document()
                        .append("newRoot", "\$distinct")
                )
        )
        val docs = ExchangeDatabase.db.getCollection(Names.Collections.modelEntities)
            .aggregate(pipeline)
            .allowDiskUse(true)

        val changedClasses = docs.map { it[Names.Fields.type].toString() }.toList()
        changedClasses.parallelStream().forEach {
            processDataMartEntityTable(batchId,operationId, it, filter, flow)
        }
    }

    private fun processDataMartEntityTable(batchId:String,operationId:String, className: String, filter: Document, flow: ExchangeFlow) {
        //todo кешировать



        val filter = BsonUtils.cloneDocument(filter)
        filter[Names.Fields.type] = className

        val collectionName = "${Names.Prefixes.dataMart}_$className"

        val pipeline = listOf(
            Document("\$match", filter),
            Document()
                .append(
                    // единичные связи добавляются как поля модели
                    "\$lookup", Document()
                        .append("from", Names.Collections.modelLinks)
                        .append(
                            "let", Document()
                                .append("id", "\$${Names.Fields.initialId}")
                        )
                        .append(
                            "pipeline",
                            listOf(
                                Document()
                                    .append(
                                        "\$match", Document()
                                            .append(
                                                "\$expr", Document()
                                                    .append(
                                                        "\$eq", listOf(
                                                            "\$${Names.Fields.fromId}",
                                                            "\$\$id"
                                                        )
                                                    )
                                            )
                                    ),
                                Document()
                                    .append(
                                        "\$match", Document()
                                            .append(
                                                "\$expr", Document()
                                                    .append(
                                                        "\$not", listOf(
                                                            "\$${Names.Fields.deletedAt}",
                                                        )
                                                    )
                                            )
                                    ),
                                Document()
                                    .append(
                                        "\$project", Document()
                                            .append("k", "\$${Names.Fields.predicate}")
                                            .append("v", "\$${Names.Fields.toId}")
                                            .append("_id", 0)
                                    )
                            ),

                            )
                        .append("as", "links")
                ),
            Document()
                .append(
                    "\$project", Document()
                        .append(Names.Fields.type, "\$${Names.Fields.type}")
                        .append(Names.Fields.id, "\$${Names.Fields.id}")
                        .append(Names.Fields.initialId, "\$${Names.Fields.initialId}")
                        .append(Names.Fields.messageId, "\$${Names.Fields.messageId}")
                        .append(Names.Fields.extId, "\$${Names.Fields.extId}")
                        .append(
                            Names.Fields.model, Document()
                                .append(
                                    "\$mergeObjects", listOf(
                                        "\$${Names.Fields.model}",
                                        Document()
                                            .append("\$arrayToObject", "\$links")
                                    )
                                )
                        )
                        .append(Names.Fields.batchId,batchId)
                        .append(Names.Fields.operationId,operationId)
                        .append(Names.Fields.changedAt, "\$\$NOW")
                        .append(Names.Fields.deletedAt,Document()
                            .append("\$cond", listOf(
                                    "\$${Names.Fields.deletedAt}",
                                    "\$\$NOW",
                                    "\$\$REMOVE"
                                )
                            )
                        )
                        .append("_id", 0)
                ),
            Document()
                .append("\$addFields", Document()
                    .append(Names.Fields.deletedModel, Document()
                        .append("\$cond", listOf(
                                "\$${Names.Fields.deletedAt}",
                                "\$${Names.Fields.model}",
                                "\$\$REMOVE",
                            )
                        )
                    )
                    .append(Names.Fields.model, Document()
                        .append("\$cond", listOf(
                            "\$${Names.Fields.deletedAt}",
                            "\$\$REMOVE",
                            "\$${Names.Fields.model}",

                        )
                        )
                    )
                ),
            Document()
                .append(
                    "\$merge", Document()
                        .append("into", collectionName)
                        .append("on", Names.Fields.initialId)
                        .append("whenMatched", "replace")
                        .append("whenNotMatched", "insert")
                )
        )

        IndexManager.create(collectionName,true,listOf(Names.Fields.id))
        IndexManager.create(collectionName,true,listOf(Names.Fields.initialId))
        IndexManager.create(collectionName,false, listOf(Names.Fields.changedAt))
        IndexManager.create(collectionName,false, listOf(Names.Fields.operationId))

        ExchangeProcessor.executeAggregate(Names.Collections.modelEntities,collectionName, pipeline, flow, batchId,operationId, "dataMart")
        CollectionChangeInfoStore.setDummy(collectionName)
    }

}