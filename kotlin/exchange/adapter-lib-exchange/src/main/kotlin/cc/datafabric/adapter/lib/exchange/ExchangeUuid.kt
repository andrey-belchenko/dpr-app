package cc.datafabric.adapter.lib.exchange

import org.bson.Document
import java.util.*

object ExchangeUuid {

    private var isTestMode:Boolean = false
    private var databaseName = "comparison"
    private var collectionName = "keys"

    fun readMode(){
        isTestMode =  ExchangeStatusStore.get(Names.Fields.uuidMode)=="test"
    }
    private val uuidsByBaseKey:MutableMap<String,String> = mutableMapOf()
    private val newMappings:MutableList<Document> = mutableListOf()

    fun loadIdsIfNeed() {
        if (!isTestMode) return;
        uuidsByBaseKey.clear()
        val items = ExchangeDatabase.getDbByName(databaseName).getCollection(collectionName).find()
        items.forEach {
            uuidsByBaseKey[it[Names.Fields.id].toString()] = it[Names.Fields.platformId].toString()
        }
    }

    fun saveIdsIfNeed() {
        if (!isTestMode) return;
        if (newMappings.any()){
            ExchangeDatabase.getDbByName(databaseName).getCollection(collectionName).insertMany(newMappings)
        }
        newMappings.clear()
    }

    fun get(base:String):String{
        if (!isTestMode){
            return  UUID.randomUUID().toString()
        }else{
            if (!uuidsByBaseKey.containsKey(base)){
                val newUuid = UUID.randomUUID().toString()
                uuidsByBaseKey[base] = newUuid
                newMappings.add(Document(mapOf(
                    Names.Fields.id to base,
                    Names.Fields.platformId to newUuid
                )))
            }
            return uuidsByBaseKey[base]!!
        }
    }
}