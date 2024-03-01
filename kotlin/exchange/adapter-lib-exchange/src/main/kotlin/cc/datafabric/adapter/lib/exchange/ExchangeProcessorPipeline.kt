package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document

object ExchangeProcessorPipeline {
    fun processFlow(batchId:String, operationId:String, flowDef: ExchangeFlow, filter: Document) {

        IndexManager.create(flowDef.input!!,false, listOf(Names.Fields.changedAt))
        IndexManager.create(flowDef.output!!,false, listOf(Names.Fields.changedAt))
        IndexManager.create(flowDef.output!!,false, listOf(Names.Fields.operationId))

        if (flowDef.mergeKey.any { it != "_id" }){
            IndexManager.create(flowDef.output!!,true, flowDef.mergeKey)
        }
        when (flowDef.operationType) {
            ExchangeFlow.OperationType.replace -> {
//                ExchangeDatabase.db.getCollection(flowDef.output!!).deleteMany(Document())
                executeInsert(batchId,operationId, flowDef, filter, true)
            }
            ExchangeFlow.OperationType.replaceWithDelete -> {
                executeInsert(batchId,operationId, flowDef, filter, true)
            }
            ExchangeFlow.OperationType.insert -> {
                executeInsert(batchId,operationId, flowDef, filter, false)
            }
            ExchangeFlow.OperationType.sync -> {
                executeUpsert(batchId, operationId, flowDef, filter)
            }
            ExchangeFlow.OperationType.syncWithDelete -> {
                executeUpsert(batchId, operationId, flowDef, filter)

            }
            else -> {}
        }
    }

    private fun executeInsert(batchId:String, operationId:String, flowDef: ExchangeFlow, filter: Document, isReplace:Boolean) {
        Logger.traceFun {

//            if (flowDef.pipeline==null){
//                val x = ""
//            }

            var pipeline = flowDef.pipeline!!
            pipeline = prepareInsertPipeline(batchId,operationId,flowDef, pipeline, filter, isReplace )
            val res =  ExchangeProcessor.executeAggregate(flowDef.input!!,flowDef.output!!, pipeline,flowDef,batchId,operationId, null)

            if (flowDef.operationType==ExchangeFlow.OperationType.replaceWithDelete){
                ExchangeDatabase.getCollection(flowDef.output!!).deleteMany(Document(Names.Fields.deletedAt,Document("\$ne",null)))
            }
            if (res.count>0){
                CollectionChangeInfoStore.setDummy(flowDef.output!!)
            }

        }
    }
    private fun executeUpsert(batchId:String, operationId:String, flowDef: ExchangeFlow, filter: Document) {
        Logger.traceFun {
            var pipeline = flowDef.pipeline!!
            pipeline = prepareUpsertPipeline(batchId, operationId,flowDef, pipeline, filter)
            val res =  ExchangeProcessor.executeAggregate(flowDef.input!!,flowDef.output!!, pipeline, flowDef, batchId,operationId, null)

            if (flowDef.operationType==ExchangeFlow.OperationType.syncWithDelete){
                ExchangeDatabase.getCollection(flowDef.output!!).deleteMany(Document(Names.Fields.deletedAt,Document("\$ne",null)))
            }
            if (res.count>0){
                CollectionChangeInfoStore.setDummy(flowDef.output!!)
            }

        }
    }

//    private fun executeDelete(flowDef:ExchangeFlowDefinition, filter: Document) {
//        Logger.traceFun {
//            var pipeline = ExchangePipelineRepository.get(flowDef.pipeline!!)
//            pipeline = prepareDeletePipeline(flowDef, pipeline, filter)
//            ExchangeProcessor.executeAggregate(flowDef.input!!, pipeline)
//            CollectionChangeInfoStore.set(flowDef.output!!)
//        }
//    }

    private fun prepareIntoExpression(into: String): Any {
        if (!into.contains(".")) {
            return into
        }
        val parts = into.split(".")
        val dbName = ExchangeDatabase.db.name + "-" + parts[0]
        return Document(
            mapOf(
                "db" to dbName, "coll" to parts[1]
            )
        )
    }

    private fun prepareInsertPipeline(batchId:String, operationId:String, flowDef: ExchangeFlow, pipeline: MutableList<Document>, filter:Document, isReplace:Boolean): MutableList<Document> {
        return Logger.traceFun {


            val preparedPipeline = preparePipeline(flowDef, pipeline, filter)
            preparedPipeline.add(Document("\$addFields", Document(Names.Fields.changedAt, "\$\$NOW")))
            val doc = Document()
                .append(Names.Fields.changedAt, "\$\$NOW")
                .append(Names.Fields.batchId, batchId)
                .append(Names.Fields.operationId, operationId)
            if (flowDef.output == Names.Collections.modelInput) {
                doc.append("_id", "\$\$REMOVE")
            }
            preparedPipeline.add(
                Document()
                    .append("\$addFields", doc)
            )
            if (isReplace) {
                preparedPipeline.add(
                    Document(
                        "\$out",
                        prepareIntoExpression(flowDef.output!!),
                    )
                )
            } else {
                preparedPipeline.add(
                    Document(
                        "\$merge",
                        Document(
                            mapOf(
                                "into" to prepareIntoExpression(flowDef.output!!),
                                "whenNotMatched" to "insert"
                            )
                        )
                    )
                )

            }

            return@traceFun preparedPipeline
        }
    }

    private fun prepareUpsertPipeline(batchId:String, operationId:String, flowDef: ExchangeFlow, pipeline: MutableList<Document>, filter:Document): MutableList<Document> {
        return Logger.traceFun {
            val preparedPipeline = preparePipeline(flowDef,pipeline, filter)
            //{ $sort: { <field1>: <sort order>, <field2>: <sort order> ... } }
            preparedPipeline.add(
                Document()
                    .append("\$addFields", Document()
                        .append(Names.Fields.changedAt, "\$\$NOW")
                        .append(Names.Fields.batchId, batchId)
                        .append(Names.Fields.operationId, operationId)
                        .append(Names.Fields.deletedAt,Document()
                            .append("\$cond", listOf(
                                "\$${Names.Fields.deletedAt}",
                                "\$\$NOW",
                                null
                            )
                            )
                        )
                    )
            )
            preparedPipeline.add(
                Document()
                    .append("\$addFields", Document()
                        .append(Names.Fields.changedAt, "\$\$NOW")
                        .append(Names.Fields.batchId, batchId)
                        .append(Names.Fields.operationId, operationId)
                        .append(Names.Fields.deletedAt,Document()
                            .append("\$cond", listOf(
                                    "\$${Names.Fields.deletedAt}",
                                    "\$\$NOW",
                                    null
                                )
                            )
                        )
                    )
            )
//            // todo нужно только когда хотим корректно обработать несколько документов с одинаковым _id (косвенный признак - есть поле _order)
//            preparedPipeline.add(
//                Document("\$group",
//                    Document(
//                        mapOf(
//                            "_id" to "\$_id",
//                            "last" to Document("\$last","\$\$ROOT")
//                        )
//                    )
//                )
//            )
//            preparedPipeline.add(
//                Document("\$replaceRoot",
//                    Document( "newRoot","\$last")
//                )
//            )

            preparedPipeline.add(
                Document("\$merge",
                    Document(
                        mapOf(
                            "on" to flowDef.mergeKey,
                            "into" to prepareIntoExpression(flowDef.output!!),
                            "whenMatched" to flowDef.whenMatched.toString(),
                            "whenNotMatched" to "insert"
                        )
                    )
                )
            )
            return@traceFun preparedPipeline
        }
    }

//    private fun prepareDeletePipeline(flowDef:ExchangeFlowDefinition, pipeline: MutableList<Document>, filter:Document): MutableList<Document> {
//        return Logger.traceFun {
//            val preparedPipeline = preparePipeline(flowDef,pipeline, filter)
//            preparedPipeline.add(
//                Document("\$project",
//                    Document(
//                        mapOf(
//                            "_id" to "\$_id",
//                            "#changedAt" to "\$\$NOW",
//                            "#isDeleted" to Document("\$toBool",true)
//                        )
//                    )
//                )
//            )
//            preparedPipeline.add(
//                Document("\$merge",
//                    Document(
//                        mapOf(
//                            "into" to flowDef.output,
//                            "whenMatched" to "merge"
//                        )
//                    )
//                )
//            )
//            return@traceFun preparedPipeline
//        }
//    }

    private  fun preparePipeline(flowDefinition: ExchangeFlow, pipeline: MutableList<Document>, filter: Document):MutableList<Document> {
        val preparedPipeline = mutableListOf<Document>()
        preparedPipeline.add(Document("\$match", filter))

//        // todo нужно не всегда, только для upsert когда несколько документов с одним _id
//        preparedPipeline.add(Document("\$sort", Document(Names.Fields.order,1)))
        pipeline.forEach {
            preparedPipeline.add(it)
        }

        if (flowDefinition.idSource!=null){
            preparedPipeline.add(
                Document()
                    .append("\$addFields", Document()
                        .append(Names.Fields.idSource,flowDefinition.idSource)
                    )
            )
        }
        return preparedPipeline
    }


}