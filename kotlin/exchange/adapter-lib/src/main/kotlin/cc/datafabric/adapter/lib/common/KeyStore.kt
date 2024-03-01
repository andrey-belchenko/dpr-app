package cc.datafabric.adapter.lib.common

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
import cc.datafabric.adapter.lib.common.tools.Stopwatch


object KeyStore {


    private val keyCollection by lazy {
        Stopwatch.start("mongo init")
        val keyDb= MongoDbClient.instance.getDatabase(Config.get("adpMongoKeysDb"))
        val keyCol = keyDb.getCollection("keyMapping")

        keyCol.createIndex(
            Indexes.ascending("sourceSystem", "sourceClassIri", "sourceKey","deletedAt"),
            IndexOptions().unique(true)
        )

        keyCol.createIndex(
            Indexes.ascending("sourceSystem", "platformKey","deletedAt"),
            IndexOptions().unique(true)
        )

        keyCol.createIndex(
            Indexes.ascending("sourceSystem", "sourceKey","deletedAt"),
        )

//        keyCol.createIndex(
//            Indexes.ascending("sourceSystem", "mrid"),
//            IndexOptions().name("mRIDBusiness").unique(true).partialFilterExpression(Filters.exists("mrid", true))
//        )
        Stopwatch.stop("mongo init")
        keyCol
    }

    private fun prepareFilter(sourceSystem:String, key:String,direction: Direction):Document{
        val params = Document()
        params["sourceSystem"] = sourceSystem
        params["deletedAt"] = BsonUtils.getMaxTimeStamp()
        when (direction) {
            Direction.Outgoing ->
                params["platformKey"] = key
            Direction.Incoming ->
                params["sourceKey"] = key
        }
        return params
    }
    fun deleteKey(sourceSystem:String, key:String, direction: Direction) {
        Logger.traceFun{
            val params = prepareFilter(sourceSystem,key,direction)
            execDeleteKey(params)
        }
    }

    //todo кешировать?
    fun queryKey(sourceSystem:String, key:String,direction: Direction): KeyInfo? {
        return Logger.traceFun{
            val params = prepareFilter(sourceSystem,key,direction)
            return@traceFun queryKey(params, direction)
        }
    }
    fun queryPlatformKey(sourceSystem:String, sourceClassIri:String, sourceKey:String): KeyInfo? {
        return Logger.traceFun{
            val params = Document()
            params["sourceSystem"] = sourceSystem
            params["sourceKey"] = sourceKey
            params["sourceClassIri"] = sourceClassIri
            params["deletedAt"] = BsonUtils.getMaxTimeStamp()
            return@traceFun queryKey(params, Direction.Incoming)
        }
    }

    private fun queryKey(params:Document,direction: Direction): KeyInfo? {
        Logger.traceObject(params)
        val result = execQueryKey(params)
        var foundKey: KeyInfo? = null
        if (result.any()) {
            val res = result.first()
            foundKey = when (direction) {
                Direction.Outgoing ->
                    KeyInfo(
                        key   =res["sourceKey"].toString(),
                        classIri = res["sourceClassIri"].toString(),
                        isGenerated = false
                    )
                Direction.Incoming ->
                    KeyInfo(
                        key   =res["platformKey"].toString(),
                        classIri = res["platformClassIri"].toString(),
                        isGenerated = false
                    )
            }
            Logger.traceData(foundKey.classIri)
            Logger.traceData(foundKey.key)
        }
        return foundKey
    }
    private fun  execQueryKey (params:Document):List<Document>{
        return Stopwatch.measureFun {
            return@measureFun keyCollection.find(params).toList()
        }
    }

    private fun  execDeleteKey (params:Document){
        Stopwatch.measureFun {
            val update = Document()
            val set = Document()
            update["\$set"]=set
            //todo как нибудь завязаться на дату транзакции
            set["deletedAt"] = BsonUtils.getCurrentTimeStamp()
            keyCollection.updateOne(params,update)
        }
    }


    fun saveKey(
        sourceSystem:String,
        origKeyInfo:KeyInfo,
        newKeyInfo:KeyInfo,
        direction: Direction) {

        when (direction) {
            Direction.Outgoing ->
                saveKey(
                    sourceSystem,
                    sourceKeyInfo=newKeyInfo,
                    platformKeyInfo=origKeyInfo
                    )
            Direction.Incoming ->
                saveKey(
                    sourceSystem,
                    sourceKeyInfo=origKeyInfo,
                    platformKeyInfo=newKeyInfo
                )
        }
    }

    fun saveKey(
        sourceSystem:String,
        sourceKeyInfo:KeyInfo,
        platformKeyInfo:KeyInfo) {
        Logger.traceFun {
            val obj = Document()
            obj["sourceSystem"] = sourceSystem
            obj["sourceClassIri"] = sourceKeyInfo.classIri
            obj["sourceKey"] = sourceKeyInfo.key
            obj["platformClassIri"] = platformKeyInfo.classIri
            obj["platformKey"] = platformKeyInfo.key
            //todo как нибудь завязаться на дату транзакции
            obj["createdAt"] = BsonUtils.getCurrentTimeStamp()
            obj["deletedAt"] = BsonUtils.getMaxTimeStamp()
            execSaveKey(obj)
        }
    }

    private fun execSaveKey (obj:Document) {
        Stopwatch.measureFun {
            keyCollection.insertOne(obj)
        }
    }

    enum class Direction {
        Outgoing, Incoming
    }


}