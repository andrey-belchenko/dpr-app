package cc.datafabric.exchange.app.processor.api


import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.UpdateOptions
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.Startup
import io.quarkus.runtime.StartupEvent
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.enterprise.event.Observes
import javax.inject.Singleton
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Singleton
@Startup
@Path("/")
class Main {


    private lateinit var executor: ExecutorService

    fun onStart(@Observes ev: StartupEvent) {
        executor = Executors.newSingleThreadExecutor()
    }

    fun onStop(@Observes ev: ShutdownEvent) {
        executor.shutdown()
    }

    @POST
    @Path("getScriptParams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getScriptParams(body: String): Response {
        val bodyDoc = Document.parse(body)
        val projectName = bodyDoc["projectName"].toString()
        val scriptName = bodyDoc["scriptName"].toString()
        val json = PythonScriptsUtils.getScriptVarJson(projectName, scriptName, "scriptParams")
        return Response.ok(json).build()
    }

    @POST
    @Path("getScriptMetadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getScriptMetadata(body: String): Response {
        val bodyDoc = Document.parse(body)
        val projectName = bodyDoc["projectName"].toString()
        val scriptName = bodyDoc["scriptName"].toString()
        val json = PythonScriptsUtils.getScriptVarJson(projectName, scriptName, "scriptMetadata")
        return Response.ok(json).build()
    }
    @POST
    @Path("exec")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun exec(body: String): Response {

        try {

            IndexManager.enable()
            Logger.traceData("/exec")
            Logger.traceData(body)
            val bodyDoc = Document.parse(body)
            //todo доделать


            val databaseName = bodyDoc["database"]?.toString() ?: Config.get(ConfigNames.mongoExchangeDb)
            // todo возможность указать БД добавлена для работы с фукционалом запуска скриптов
            //  решение сомнительное и реализовано не для всех типов команд
            val db =  ExchangeDatabase.getDbByName(databaseName)
            val commands = (bodyDoc["commands"] as List<*>).map { it as Document }
            if (bodyDoc["isAsync"] != true) {
                val output = execCommands(commands,db)
                return Response.ok(BsonUtils.toJsonString(Document("data", output))).build()
            } else {
                val asyncExecutionId = UUID.randomUUID().toString()
                saveAsyncExecStatus(asyncExecutionId, ExecStatus.started, db)
                executor.execute {
                    try {
                        val output = execCommands(commands,db)
                        saveAsyncExecStatus(asyncExecutionId, ExecStatus.completed,db, output)
                    } catch (ex: Exception) {
                        saveAsyncExecStatus(asyncExecutionId, ExecStatus.error, db,null, ex)
                        ExchangeErrorLogger.log(ex.message, ex.stackTraceToString())
                    }
                }
                val output = Document("asyncExecutionId", asyncExecutionId)
                return Response.ok(BsonUtils.toJsonString(output)).build()
            }

        } catch (ex: Exception) {
            try {
                ExchangeErrorLogger.log(ex.message, ex.stackTraceToString())
                throw ex
            } catch (_: Exception) {
                throw ex
            }


        }

    }

   private enum class ExecStatus {
        started,  completed , error
    }
   private fun saveAsyncExecStatus(id:String, execStatus:ExecStatus,database: MongoDatabase, output:Iterable<Document>?=null,exception: Exception?=null) {
       database.getCollection(Names.Collections.asyncWebOperations).createIndex(
           Indexes.ascending(
               listOf(
                   Names.Fields.id
               )
           )
       )
       val filter = Document(Names.Fields.id, id)
       val value = Document(
           mapOf(
               Names.Fields.id to id,
               "status" to execStatus.toString(),
           )
       )
       if (output != null) {
           value["output"] = output
       }
       if (exception != null) {
           value["error"] = exception.toString() + "\n"+exception.stackTraceToString()
       }

       if (execStatus == ExecStatus.started){
           value["startedAt"] = ExchangeTimestamp.now()
       }
       if (execStatus == ExecStatus.completed || execStatus == ExecStatus.error ){
           value["finishedAt"] = ExchangeTimestamp.now()
       }
       val col = database.getCollection(Names.Collections.asyncWebOperations)
       col.updateOne(filter, Document("\$set", value), UpdateOptions().upsert(true))
   }

    fun execCommands(commands:Iterable<Document>, database:MongoDatabase): MutableList<Document> {
        val output = mutableListOf<Document>()

        commands.forEach {

            val command = it["command"].toString()
            when (command) {
                "merge" -> {
                    val mergeCols = mutableListOf<String>()
                    val on = it["on"]
                    if (on is String) {
                        mergeCols.add(on)
                    } else if (on is List<*>) {
                        on.forEach { v ->
                            mergeCols.add(v.toString())
                        }
                    }
                    if (mergeCols.size > 1 || mergeCols[0] != "_id") {
                        IndexManager.create(database ,it["into"].toString(), true, mergeCols)
                        Logger.status("Index was created")
                    } else {
                        Logger.status("Index was not created")
                    }
                    val mergeOptions = mutableMapOf(
                        "into" to it["into"],
                        "on" to it["on"],
                    )
                    if (it["whenMatched"] != null) {
                        mergeOptions["whenMatched"] = it["whenMatched"]
                    }
                    if (it["whenNotMatched"] != null) {
                        mergeOptions["whenNotMatched"] = it["whenNotMatched"]
                    }
                    val pipeline = listOf(
                        Document(
                            "\$addFields",
                            Document("data", it["data"])
                        ),
                        Document("\$unwind", "\$data"),
                        Document("\$replaceRoot", Document("newRoot", "\$data")),
                        Document(
                            "\$addFields",
                            Document(Names.Fields.changedAt, "\$\$NOW")
                        ),
                        Document(
                            "\$merge",
                            Document(mergeOptions)
                        )
                    )
                    ExchangeDummy.initialize(database)
                    database.getCollection(Names.Collections.dummy)
                        .aggregate(pipeline)
                        .allowDiskUse(true)
                        .toCollection()

                }
                "find" -> {
                    val result = database
                        .getCollection(it["collection"].toString())
                        .find(it["filter"] as Document)
                    output.addAll(result)
                }
                "trigger" -> {
                    val changedCollection = ExchangeListener.ChangedCollection(
                        name = it["trigger"].toString(),
                        filter = it["filter"] as Document
                    )
                    ExchangeProcessor.action(listOf(changedCollection))
                }
                "runScript" -> {
                    runScript(it)
                }
            }
        }

        output.forEach { doc ->
            BsonUtils.getDocumentsRecursive(doc).toList().forEach { obj ->
                obj.document.toMap().forEach { prop ->
                    if (prop.value is ObjectId || prop.value is Date) {
                        obj.document[prop.key] = prop.value.toString()
                    }
                }
            }
        }
        return output
    }

    fun runScript(options: Document) {
        val projectName = options["projectName"].toString()
        val scriptName = options["scriptName"].toString()
        val params = options["params"] as? Document
        PythonScriptsUtils.runScript(projectName, scriptName, params)
    }








}