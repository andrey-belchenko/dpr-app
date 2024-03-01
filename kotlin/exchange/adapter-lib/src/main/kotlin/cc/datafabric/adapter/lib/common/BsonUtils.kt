package cc.datafabric.adapter.lib.common

import org.bson.BsonDateTime
import org.bson.Document
import org.bson.json.JsonWriterSettings
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

object BsonUtils {
    fun getProperties(document: Document) = sequence {
        document.iterator().forEach {
            yield(BsonProp(document, it.key, it.value, it.value is List<*>))
        }
    }

    // очень сложно продиагностировать проблемы, при ошибке не показывает место, где она реально происходит
    fun getDocumentsRecursive(document: Document,parentProp:BsonProp?=null): Sequence<BsonDocument> = sequence {
        yield(BsonDocument(document,parentProp))
        getProperties(document).forEach {
            yieldAll(getDocumentsRecursiveFromValue(it.value,it))
        }
    }
    fun valueAsDocuments(value: Any?) = sequence{
        when ( value ) {
            is Document -> yield(value)
            is List<*> -> value.forEach { doc ->
                if (doc is Document) {
                    yield(doc)
                }
            }
        }
    }


    fun getDocumentsRecursiveFromValue(value: Any?,parentProp:BsonProp?=null) = sequence {
        valueAsDocuments(value).forEach {
            yieldAll(getDocumentsRecursive(it,parentProp))
        }
    }

    fun toJsonString(document: Document):String{
        return document.toJson(
            JsonWriterSettings
                .builder()
                .indent(true)
                .build()
        )
    }

    //todo не разобрался что тут с зонами и в целом с датами в bson/mongo, проверить сделать нормально

    fun getTimeStampFromValue(value: Date?): BsonDateTime? {

        if (value==null){
            return  null
        }
        return BsonDateTime(value.toInstant().toEpochMilli())
    }
    fun getCurrentTimeStamp(): BsonDateTime {
        return BsonDateTime(ZonedDateTime.now().toInstant().toEpochMilli())
    }

    private val maxBsonDate = BsonDateTime(ZonedDateTime.parse("3000-01-01T00:00Z").toInstant().toEpochMilli())
    fun getMaxTimeStamp(): BsonDateTime {
        return maxBsonDate
    }

    private val minBsonDate = BsonDateTime(ZonedDateTime.parse("2000-01-01T00:00Z").toInstant().toEpochMilli())
    fun getMinTimeStamp(): BsonDateTime {
        return minBsonDate
    }

    fun cloneDocument(document: Document):Document{
        val obj = Document()
        document.forEach{
            obj[it.key] = it.value
        }
        return obj
    }


    fun valueToString(value:Any?):String?{
        val result = if (value is Date){
            val localOffsetDate  = value.toInstant().atOffset(OffsetDateTime.now().offset)
            val  utcOffsetDate =   localOffsetDate.withOffsetSameInstant(ZoneOffset.UTC)
            utcOffsetDate.format(DateTimeFormatter.ISO_DATE_TIME)
        }else{
            value.toString()
        }
        return  result
    }


    class BsonDocument(
        val document: Document,
        val parentProp:BsonProp?
    )

    class BsonProp(
        val document: Document,
        val name: String,
        val value: Any?,
        val isArray: Boolean
    )
}