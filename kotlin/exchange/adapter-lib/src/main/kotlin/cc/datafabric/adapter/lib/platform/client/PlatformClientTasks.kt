package cc.datafabric.adapter.lib.platform.client


import cc.datafabric.adapter.lib.common.TimeUtils.toFormattedString
import cc.datafabric.adapter.lib.sys.*
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.bson.Document
import java.time.ZonedDateTime
import java.util.*
import cc.datafabric.adapter.lib.common.ConfigNames
import com.google.protobuf.Message
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object PlatformClientTasks: PlatformClientBase() {


    fun getDiff(startTimestamp: ZonedDateTime, endTimestamp: ZonedDateTime):String {
        return Logger.traceFun {
            val taskInfo = postExportTask(startTimestamp, endTimestamp)
            val taskInfoObj = waitTaskCompletion(taskInfo)
            return@traceFun getDiffFile(taskInfoObj["resultFile"].toString())
        }
    }

    fun sendDiff(diffData:ByteArray){
        Logger.traceFun {
            val taskInfo = postImportTask(diffData)
            waitTaskCompletion(taskInfo)
        }
    }
    fun waitTaskCompletion(taskInfo:String):Document{
        return Logger.traceFun {
            var taskInfoStr = taskInfo
            var taskInfoObj:Document? = null
            try {
                 taskInfoObj =  Document.parse(taskInfoStr)
            } catch (e: Exception) {
                 throw Exception("Can't parse response. ${e.message}\n$taskInfoStr")
            }



            val taskId = taskInfoObj["taskId"].toString()
            var status = taskInfoObj["status"].toString()
            while (status in listOf("RUNNING","STARTING")) {
                taskInfoStr = getTask(UUID.fromString(taskId))
                taskInfoObj = Document.parse(taskInfoStr)
                status = taskInfoObj["status"].toString()
                Thread.sleep(1000)
            }
            if (status != "COMPLETED") {
                throw Exception("Task execution failed\n$taskInfoStr")
            }
            return@traceFun taskInfoObj!!
        }
    }





    private fun getDiffFile(relativeUrl: String ): String {
        return Logger.traceFun {
            return@traceFun sendRequest(HttpGet(), relativeUrl)
        }
    }

    private fun getTask(id: UUID): String {
        return Logger.traceFun {
            return@traceFun sendRequest(HttpGet(), "tasks/$id")
        }
    }

    private fun postImportTask(diffData:ByteArray): String {
        return Logger.traceFun {
            val taskName = UUID.randomUUID().toString()
            val zonedDateTime = ZonedDateTime.now()
            val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)
            var formattedDateTime = utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))


//            formattedDateTime = "2023-06-28T14:25:01.000Z"
//            formattedDateTime = "2022-09-08T12:08:00.000Z"
//            formattedDateTime = "2023-06-28T11:47:15.000Z"
//            println(formattedDateTime)

            val requestBody = """
                {
                  "taskName": "$taskName",
                  "parameters": {
                    "profileVersionIRI": "${Config.get(ConfigNames.profileVersionIri)}",
                    "createTimestamp": "$formattedDateTime"
                  }
                }
            """.trimIndent()
            val builder = MultipartEntityBuilder.create()
            builder.addBinaryBody("file", diffData, ContentType.APPLICATION_OCTET_STREAM, "diff.xml")
            builder.addTextBody("task", requestBody, ContentType.APPLICATION_JSON)
            val request = HttpPost()
            request.entity = builder.build()
            return@traceFun sendRequest(request, "tasks/import")
        }
    }

    private fun postExportTask(startTimestamp: ZonedDateTime, endTimestamp: ZonedDateTime): String {
        //todo сейчас в качестве метки на которую выгружаются данные используется timestamp,
        //нужно вместо этого предусмотреть инкрементальный ID транзакции
        //чтобы не было разрывов и пересечений между диффами
        return Logger.traceFun {
            val taskName = UUID.randomUUID().toString()
            val requestBody = """
            {
              "taskName": "$taskName",
              "parameters": {
                "profileVersionIRI": "${Config.get(ConfigNames.profileVersionIri)}",
                "startTimestamp": "${startTimestamp.toFormattedString()}",
                "endTimestamp": "${endTimestamp.toFormattedString()}"
              }
            }
            """.trimIndent()
            val request = HttpPost()
            request.setHeader("Content-Type", "application/json")
            request.entity = StringEntity(requestBody)
            return@traceFun sendRequest(request, "tasks/export")
        }
    }

    private fun sendRequest(request: HttpRequestBase, relativeUrl: String): String {
        return Logger.traceFun (relativeUrl) {
            request.setHeader("X-Agent-IRI", "http://user-space.ex#${Config.get("adpName")}")
            return@traceFun sendRequestBase(request,relativeUrl)
        }
    }
}