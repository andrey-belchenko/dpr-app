package cc.datafabric.adapter.lib.general

import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.*
import java.lang.Exception
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
import cc.datafabric.adapter.lib.common.tools.Stopwatch

object PipelineProcessor {


    private val servDb by lazy {
        Stopwatch.start("mongo init")
        val res = MongoDbClient.instance.getDatabase(Config.get("adpMongoServDb"))
        Stopwatch.stop("mongo init")
        res
    }
    private val dummyCollection by lazy {
        Stopwatch.start("mongo init")
        val dummyCol= servDb.getCollection("dummy")
        if (dummyCol.countDocuments() != 1L) {
            dummyCol.deleteMany(Document())
            dummyCol.insertOne(Document())
        }
        Stopwatch.stop("mongo init")
        dummyCol

    }


    fun exec(message: IncomingInMessage, pipelineName: String): Iterable<IncomingOutMessage> {
        val pipelineResult = exec(message.asObject(), pipelineName)
        if (!pipelineResult.any()) {
            throw Exception("Pipeline returned empty result")
        }
        return pipelineResult.map { IncomingOutMessage.fromPipelineResult(it, true) }
    }

    fun exec(message: OutgoingInMessage, pipelineName: String): Iterable<OutgoingOutMessage> {
        val pipelineResult = exec(message.asObject(), pipelineName)
        if (!pipelineResult.any()) {
            throw Exception("Pipeline returned empty result")
        }
        return pipelineResult.map { OutgoingOutMessage.fromPipelineResult(it) }
    }

    private fun exec(message:Document,pipelineName:String):Iterable<Document> {
        val pipeline = PipelineRepository.get(pipelineName)
        Logger.traceBeg("preparing pipeline")
        // в первый шаг пайплайна подставляется входящее сообщение
        // пайплайн выполняется на таблице с одним пустым документом
        pipeline[0] = Document(mapOf("\$project" to message))
        Logger.traceObject(pipeline)
        return exec(pipeline)
    }

    private fun exec(pipeline: MutableList<Document>): Iterable<Document> {
        Logger.traceFunBeg()
        Stopwatch.start("pipeline execution")
        val result = dummyCollection.aggregate(pipeline).toList()
        Stopwatch.stop("pipeline execution")
        Logger.traceFunEnd()
        Logger.traceObject(result)
        return result
    }


}