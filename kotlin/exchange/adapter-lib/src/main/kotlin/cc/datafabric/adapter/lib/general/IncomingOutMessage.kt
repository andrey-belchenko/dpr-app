package cc.datafabric.adapter.lib.general

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.sys.*
import org.bson.Document
import java.text.SimpleDateFormat
import java.util.*


/**
 * Incoming message converted to the common profile. Used for sending data from the adapter to the platform
 */
class IncomingOutMessage constructor(private val document:Document, val isWithSourceKeys:Boolean) {
    internal fun asObject() = document
    fun getHeader()=document["m:Header"] as Document
    fun getContext()=(document["@context"] as Document)
    internal fun getNamespaceUri(prefix:String)= getContext()[prefix].toString()
    internal fun getPayload()=document["m:Payload"]

    internal fun getPayloadDocuments():Sequence<Document>{
            return BsonUtils.valueAsDocuments(getPayload())
    }

//    init {
//        // to do пока убираю, это вроде как дефолтные namespace и они сейчас не используются,
//        // но их наличие усложняет обработку diff.ToXMl
//        // разобраться доработать при необходимости, проверить
//
//        // оказалось так нельзя, ядро ломается
//
////        getContext().remove("@base")
////        getContext().remove("@vocab")
//    }

    companion object {
        internal fun fromPipelineResult(value: Document, isWithSourceKeys:Boolean): IncomingOutMessage {
            return IncomingOutMessage(value, isWithSourceKeys)
        }

        fun fromString(value: String, isWithSourceKeys:Boolean): IncomingOutMessage {
            val doc = Document.parse(value)
            return IncomingOutMessage(doc, isWithSourceKeys)
        }



        private val  msgTemplate = """
            {
              "@context": {
                "m": "http://www.iec.ch/TC57/2011/schema/message",
                "xsd": "http://www.w3.org/2001/XMLSchema#",
                "astu": "http://ontology.adms.ru/UIP/md/2021-1#",
                "pl": "http://ontology.adms.ru/UIP/md/2021-1#",
                "@vocab": "http://ontology.adms.ru/UIP/md/2021-1#",
                "@base": "http://ontology.adms.ru/UIP/md/2021-1#"
              },
              "@type": "pl:createSubstation",
              "@id": "0e84617d-6df6-4d7b-945c-b7e0caedf590",
              "m:Header": {
                "m:Noun": null,
                "m:Verb": null,
                "m:MessageID": null,
                "m:Timestamp": {
                  "@type": "xsd:dateTime",
                  "@value": null
                },
                "m:Source": null
              },
              "m:Payload": null
            }
        """.trimIndent()

        //todo костыль
        fun fromDocumentArray(payload: Iterable<Document>): IncomingOutMessage {
            val doc = Document.parse(msgTemplate)
            doc["m:Payload"] =  payload
            return IncomingOutMessage(doc, false)
        }
    }




    fun getEntities():Sequence<IncomingOutMessageEntity> = sequence {
        BsonUtils.getDocumentsRecursiveFromValue(getPayload()).forEach {
            yield(IncomingOutMessageEntity(this@IncomingOutMessage,it.document))
        }
    }

    //для аккумуляции сообщений если приходит новое сообщение с тем же Id старое необработанное заменяется
    fun mainEntityId():String {
        // предполагается что можно ориентироваться на первый объект в сообщении, но может быть это не так
        return getEntities().first().getSourceKey()!!
    }



    fun setNoun(value: String) {
        getHeader()["m:Noun"] = value
    }

    fun setSource(value: String) {
        getHeader()["m:Source"] = value
    }
    fun getSource(): String {
        return getHeader()["m:Source"].toString()
    }

    fun setVerb(value: String) {
        getHeader()["m:Verb"] = value
    }

    fun getVerb():String {
      return   getHeader()["m:Verb"].toString()
    }
    fun setType(value: String) {
        document["@type"] = value
    }

    fun updateTimestamp(){
        val timestampVal =  getHeader()["m:Timestamp"] as Document
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(Date())
        timestampVal["@value"] = date
    }

    fun generateId(){
        Logger.traceFunBeg()
        val id = UUID.randomUUID().toString()
        getHeader()["m:MessageID"] = id
        document["@id"] = id
        Logger.traceFunEnd()
    }

    fun getId():String {
        Logger.traceFunBeg()
        val value =  getHeader()["m:MessageID"]
        Logger.traceFunEnd()
        return value.toString()
    }

    fun getText():String {
        Logger.traceFunBeg()
        val value = BsonUtils.toJsonString(document)
        Logger.traceFunEnd()
        return value
    }

    fun getType():String{
        Logger.traceFunBeg()
        val value =  document["@type"]
        Logger.traceFunEnd()
        return value.toString()
    }



}