package cc.datafabric.extensions

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import org.bson.Document

object ContextProcessor {
    class ExtId (
       val idSource:String,
       val id:String
    )
    fun parse(string: String):Document{
        return  Document.parse(string)
    }

    fun extractIds(document: Document):Iterable<String> {
        val ids = mutableSetOf<String>()
        BsonUtils.getDocumentsRecursiveFromValue(document["m:Payload"]).forEach {
            val value = it.document["@id"]
            if (value != null) {
                val id = value.toString().split("#")[1]
                if (!ids.contains(id)){
                    ids.add(id)
                }
            }
        }
        return ids
    }

    private fun createExtIdObj(iri:String,idMap:Map<String,ContextProcessor.ExtId>) :Document{
        val id = iri.split("#")[1]
        val extIdObj = Document("platform",id)
        if (idMap.containsKey(id)) {
            val extId = idMap[id]!!
            extIdObj[extId.idSource] = extId.id
                // откуда то приходят доп кавычки
                .replace("\"","") //
        }
        return extIdObj
    }

    private fun createDocValue(oldValue:Document,idMap:Map<String,ContextProcessor.ExtId>) :Document{

        return  Document(
            mapOf(
                "@id" to createExtIdObj(oldValue["@id"].toString(),idMap),
                "@type" to  oldValue["@type"].toString().split("#")[1],
                "@action" to "link"
            )
        )
    }

    fun convertToModelInput(document: Document,idMap:Map<String,ContextProcessor.ExtId>):Document {
        val inDocs =  document["m:Payload"] as List<Document>
        val outDocs = mutableListOf<Document>()
        inDocs.forEach { inDoc ->
            val outDoc = Document()
            outDoc["@id"] =  createExtIdObj(inDoc["@id"].toString(),idMap)
            outDoc["@type"] = inDoc["@type"].toString().split("#")[1]
            outDoc["@action"] = "create"
            inDoc.forEach { fieldName, fieldValue ->
                if (!fieldName.startsWith("@") && !fieldName.contains(":")){
                    val newFieldName = fieldName.replace(".","_")
                    if (fieldValue is Document){
                        outDoc[newFieldName] =createDocValue(fieldValue,idMap)
                    }
                    else if (fieldValue is List<*>){
                        val newValue = mutableListOf<Document>()
                        fieldValue.forEach { valItem->
                            if (valItem is Document){
                                newValue.add(createDocValue(valItem,idMap))
                            }
                        }
                        outDoc[newFieldName] = newValue
                    }else{
                        outDoc[newFieldName] = fieldValue
                    }
                }
            }
            outDocs.add(outDoc)
        }
        return Document("model" , outDocs)
    }
}