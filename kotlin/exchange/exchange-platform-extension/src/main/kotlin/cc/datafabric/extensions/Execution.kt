package cc.datafabric.extensions

import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.core.Profile
import cc.datafabric.services.*
import cc.datafabric.services.entities.VocabularyEntity
import com.mongodb.client.model.UpdateOptions
import org.bson.Document
import java.util.*
import kotlin.system.measureTimeMillis

class Execution(
    private val extensionConfig: Properties,
    private val semQueryService: SemqueryService,
    private val vocabulariesService: VocabulariesService,
    private val profilesService: ProfilesService,
    private val message: String
)  {

    companion object {
        private val contextSettings:MutableMap<String,Document> = mutableMapOf()
        private var profile:Profile? = null
    }
    private lateinit var msgDoc:Document
    private lateinit var messageType:String
    private var contextSetting:Document? = null
    private val outputCollections = mutableListOf<String>()
    private val result = mutableMapOf<String,String>()
    private val operationLog = mutableListOf<Document>()

    // для обработки контекста
    private val contextQueriesParams = mutableMapOf<String,Set<String>>()
    private val contextQueriesResults = mutableListOf<String>()
    private val contextDocs = mutableListOf<Document>()
    private val allContextIds = mutableListOf<String>()
    private val idMap = mutableMapOf<String,ContextProcessor.ExtId>()
    private val modelDocs = mutableListOf<Document>()
    private val summary = Document()
    fun run() {
        var platformTime: Long = 0

        val totalTime = measureTimeMillis {
            initProfile()
            initExchangeSettings()
            msgDoc = Document.parse(message)

            messageType = msgDoc["КодСобытия"].toString()
            loadContext()
            var time = measureTimeMillis {
                skipChanges()
                processMessage()
            }
            logOperation(true, "ExecuteRules", time)
            time = measureTimeMillis {
                buildResult()
            }
            logOperation(true, "PrepareResult", time)
            addResult("OperationLog", BsonUtils.toJsonString(Document("items", operationLog)))

            operationLog.forEach {
                if (it["scope"] == "platform") {
                    platformTime += it["timeMs"] as Long
                }
            }
        }
        val dataSets =  mutableListOf("Summary")
        result.forEach {
            dataSets.add(it.key)
        }
        addSummaryItem("totalTimeMs", totalTime)
        addSummaryItem("platformSideTimeMs", platformTime)
        addSummaryItem("resultDataSetsNames", dataSets)
        addResult("Summary",BsonUtils.toJsonString(summary))
    }

    fun getResult():MutableMap<String,String> {
        return result
    }

    private fun addSummaryItem(name:String,value: Any){
        summary[name] = value
    }
    private fun logOperation(isExtensionScope:Boolean, operation:String, timeMs:Long, count:Int?=null){
        val item = Document()
        item["scope"] =  if (isExtensionScope) "extension" else "platform"
        item["operation"] = operation
        item["timeMs"] = timeMs
        if (count!=null){
            item["count"] = count
        }
        operationLog.add(item)
    }
    private fun buildResult() {
        outputCollections.forEach { name ->
            when (name) {
                "out_Sk11" -> {
                    buildXmlResult(name, true)
                }
                "out_Platform" -> {
                    buildXmlResult(name, false)
                }
                else -> {
                    buildJsonResult(name)
                }
            }
        }

    }

    private  fun addResult(name:String, value:String) {
        result[name.replace("out_", "")] = value
    }

    private fun buildXmlResult(collectionName:String, isSk:Boolean){
        val docs =  ExchangeDatabase.getCollection(collectionName).find().toList()
        val diff = ExchangeDiffSerializer.getDiffXml(docs, isSk)
        addResult(collectionName,diff)
        val keyMappings = mutableListOf<Document>()
        val checkSet = mutableSetOf<String>()
        if (!isSk){
            val ids = mutableSetOf<String>()

            docs.forEach { doc ->
                val id = doc["id"].toString()
                if (!ids.contains(id)){
                    ids.add(id)
                }
                if (doc["extId"] != null) {
                    val extId = doc["extId"] as Document
                    extId.forEach { name, value ->
                        if (name != "platform") {
                            val checkKey = "$id-$name-$value"
                            if (!checkSet.contains(checkKey)){
                                checkSet.add(checkKey)
                                val keyMapping =  Document(
                                    mapOf(
                                        "iri" to id,
                                        "externalSystemId" to name,
                                        "document" to Document("externalKey",value)
                                    )
                                )
                                keyMappings.add(keyMapping)
                            }
                        }
                    }
                }
            }
            addSummaryItem("affectedEntitiesCount",ids.count())
            addResult("KeyMapping",BsonUtils.toJsonString(Document("items",keyMappings)) )
        }
    }

    private fun buildJsonResult(collectionName:String){
       val docs =  ExchangeDatabase.getCollection(collectionName).find().toList()
       val fullDoc = Document("messages",docs)
       val fullStr = BsonUtils.toJsonString(fullDoc)
       addResult(collectionName,fullStr)
    }

    private fun skipChanges() {
        val processorName = Config.get(ConfigNames.processorName)
        val filter = Document("processorName", processorName)
        val date = ExchangeTimestamp.now()
        val data = Document(
            mapOf(
                "processorName" to processorName,
                "value" to date
            )
        )
        ExchangeDatabase.getCollection("sys_ProcessorTimestamp").updateOne(
            filter,
            Document("\$set", data),
            UpdateOptions().upsert(true)
        )
        val docs = ExchangeDatabase.getCollection("sys_CollectionChangeInfo").find(Document())
        docs.forEach { doc ->
            filter["collectionName"] = doc["collectionName"]
            data["collectionName"] = doc["collectionName"]
            ExchangeDatabase.getCollection("sys_ProcessorCollectionTimestamp").updateOne(
                filter,
                Document("\$set", data),
                UpdateOptions().upsert(true)
            )
        }
    }


    private fun processMessage(){
        ExchangeStore.useTimer =  false
        ExchangeStore.put(message)
        ExchangeStore.applyInsert()
        ExchangeListener.getChanges(listOf("in_$messageType")){
            ExchangeProcessor.action(it.changedCollections)
        }
        var hasChanges = true
        while (hasChanges){
            hasChanges = false
            ExchangeListener.getChanges(){
                hasChanges = it.changedCollections.any()
                it.changedCollections.forEach { col->
                    if (col.name.startsWith("out_")){
                        outputCollections.add(col.name)
                    }
                }
                ExchangeProcessor.action(it.changedCollections)
            }
        }

    }

    private fun initProfile(){
        if (profile==null) {
            val time = measureTimeMillis {
                profile = profilesService.getProfile(extensionConfig["profile-version"].toString(), null)
            }
            val exchangeProfileSourcePlatform = ExchangeProfileSourcePlatform(profile!!)
            ExchangeProfile.profileSource = exchangeProfileSourcePlatform
            ExchangeProfile.initialize()
            logOperation(false,"loadProfile",time)
        }
    }
    private fun updateSettingsCache(){
        contextSettings.clear()
        ExchangeSettingsRepository.getContextSettings()?.forEach {
            contextSettings[it["messageType"].toString()] = it
        }
        IndexManager.reset()
        ViewManager.createViews()
    }
    private fun initExchangeSettings(){
        var reloaded: Boolean
        val time  = measureTimeMillis {
            reloaded = ExchangeSettingsRepository.loadIfNeed()
            if (reloaded) {
                updateSettingsCache()
            }
        }
        if (reloaded){
            logOperation(true,"initExchangeSettings",time)
        }
    }

    private fun loadContext(){
        contextSetting = contextSettings[messageType]
        if (contextSetting != null) {
            loadContextQueryRootIrisByMessageKeys()
            loadContextExecuteSemQueries()
            loadContextParseQueryResultsAndExtractIris()
            loadContextQueryExtKeysByContextIris()
            loadContextConvertContext()
            loadContextImportToExchangeDb()
        }
    }

    private fun loadContextImportToExchangeDb() {
        val time = measureTimeMillis {
            clearDatabase()
            ExchangeProcessor.modelInput(modelDocs)
            CollectionChangeInfoStore.commit()
        }
        logOperation(true,"loadContext-ImportToExchangeDb",time)
    }

    private fun loadContextConvertContext() {

        val time = measureTimeMillis {
            contextDocs.forEach { contextDoc ->
                val modelInputDoc = ContextProcessor.convertToModelInput(contextDoc, idMap)
                modelDocs.add(modelInputDoc)
            }
        }
        logOperation(true,"loadContext-ConvertContext",time)
    }

    private fun loadContextQueryExtKeysByContextIris() {

        val time = measureTimeMillis {
            val idSource = contextSetting!!["idSource"].toString()
            allContextIds.forEach { id ->
                try {
                    val mapping = vocabulariesService.getByIriAndExternalSystemId(id, idSource)
                    val extId = ContextProcessor.ExtId(idSource, mapping.document["externalKey"].toString())
                    idMap[id] = extId
                } catch (e: Exception) {
                    // выдает ошибку при отсутствии ключа, не нашел возможности по другому проверить наличие
                }
            }
        }
        logOperation(false,"loadContext-QueryExtKeysByContextIris",time,allContextIds.count())
        addSummaryItem("contextEntitiesCount", allContextIds.count())
    }

    private fun loadContextParseQueryResultsAndExtractIris() {

        val time = measureTimeMillis {
            contextQueriesResults.forEach { contextStr ->
                val contextDoc = ContextProcessor.parse(contextStr)
                contextDocs.add(contextDoc)
                val contextIds = ContextProcessor.extractIds(contextDoc)
                allContextIds.addAll(contextIds)
            }
        }
        logOperation(true,"loadContext-ParseQueryResultsAndExtractIris",time,allContextIds.count())
    }

    private fun loadContextExecuteSemQueries() {
        val time = measureTimeMillis {
            contextQueriesParams.forEach {
                val contextStr = semQueryService.executeSemanticQuery(
                    semQueryIri = it.key,
                    rootIris = it.value
                )
                contextQueriesResults.add(contextStr)
            }
        }
        logOperation(false,"loadContext-ExecuteSemQueries",time,contextQueriesParams.count())

    }

    private fun loadContextQueryRootIrisByMessageKeys() {
        val contextQueries = contextSetting!!["contextQueries"] as List<Document>
        var fullTime:Long = 0
        var count = 0
        contextQueries.forEach { contextQuery ->
            val extRootIds = mutableListOf<String>()
            val rootIdsExprs = contextQuery["rootIds"] as List<String>
            rootIdsExprs.forEach { rootIdsExpr ->
                BsonPath.evaluate(msgDoc, rootIdsExpr).forEach {
                    extRootIds.add(it)
                }
            }
            val mappingQuery = mutableListOf<Map<String, String>>()
            extRootIds.forEach {
                mappingQuery.add(mapOf("externalKey" to it))
            }
            var mappings:List<VocabularyEntity>
            val time = measureTimeMillis {
                 mappings = vocabulariesService.findByFields(mappingQuery)
            }
            fullTime+=time
            count+=mappingQuery.count()
            val rootIds = mappings.map { it.iri }.toSet()
            if (rootIds.any()) {
                contextQueriesParams[contextQuery["queryId"] as String] = rootIds
            }
        }
        logOperation(false,"loadContext-QueryRootIrisByMessageKeys",fullTime,count)
    }

    private fun clearDatabase() {
        val names = ExchangeDatabase.getCollectionNames()
        val skip = listOf(
            "sys_MessageLog",
            "sys_PipelineLog",
            "sys_Errors",
            "sys_StatusStore"
        )
        names.forEach {
            if (!skip.contains(it)) {
                ExchangeDatabase.clearCollection(it)
            }
        }
    }



}