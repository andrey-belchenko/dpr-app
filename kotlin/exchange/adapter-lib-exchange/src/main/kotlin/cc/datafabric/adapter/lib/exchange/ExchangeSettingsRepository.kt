package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
import org.bson.Document
import java.io.File
import cc.datafabric.adapter.lib.sys.*
import java.nio.file.Paths
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.exchange.flows.ExchangeRule
import com.mongodb.client.gridfs.GridFSBuckets
import org.bson.types.ObjectId
import kotlin.io.path.Path

object ExchangeSettingsRepository {

    private val flowsBySource = mutableMapOf<String, MutableList<ExchangeRule>>()
    private val flowsByTags = mutableMapOf<String, MutableList<ExchangeRule>>()
    private val flowsByName = mutableMapOf<String, ExchangeRule>()
    val views = mutableListOf<ExchangeFlow>()
    val cascadeDeleteLinks = mutableListOf<String>()



    fun getBySource(source: String): Iterable<ExchangeRule> {
        if (flowsBySource[source] != null) {
            return flowsBySource[source]!!
        }
        return listOf()
    }

    fun getByName(name: String): ExchangeRule? {

        return flowsByName[name]
    }

    fun getByTags(tags: Iterable<String>): Iterable<ExchangeRule> {
        val items =  mutableSetOf<ExchangeRule>()
        tags.forEach { tag->
                if (flowsByTags.containsKey(tag)){
                    flowsByTags[tag]!!.forEach {
                        if (!items.contains(it)){
                            items.add(it)
                        }
                    }
                }
        }
        return items
    }


    val builderDir = Config.tryGet(ConfigNames.exchangeBuilderPath)

    private val initialSettingsDir = Config.get(ConfigNames.exchangeSettingsPath)

    // private val configurationDir = Path(initialSettingsDir).parent.toString()
    // 2. и еще добавилась особенность связанная с подключением расширения для платформы
    // в отличие от первоначального варианта initialSettingsDir ссылается уже на главную папку, а не на вложенную settings
    private val configurationDir by lazy {
        var res = initialSettingsDir
        if (File(initialSettingsDir).exists()) {
            if (File(Paths.get(initialSettingsDir, "rules").toString()).exists()) {
                res = Path(initialSettingsDir).parent.toString()
            }
        }
        res
    }
    // 1. этот вариант добавлялся позже, поэтому такая химия
    // настройки могут передаваться из БД, архив с настройками распаковывается в папку exchangeSettingsPath/../temp/settings
    val tempDir = Paths.get(configurationDir, "temp").toString()
    private val tempSettingsDir = Paths.get(tempDir, "settings").toString()

    fun getSettingsDir(): String {
        return if (hasDbSettings()) {
            tempSettingsDir
        } else {
            initialSettingsDir
        }
    }

    private fun getRulesSettingsDir(): String {
        return Paths.get(getSettingsDir(), "rules").toString()
    }

    fun getProfileSettingsDir(): String {
        return Paths.get(getSettingsDir(), "profile").toString()
    }
//    private val rulesSettingsDir = Paths.get(  settingsDir,"rules").toString()
//    private val profileSettingsDir = Paths.get(  Config.get(ConfigNames.exchangeSettingsPath),"profile").toString()

    //adpDevMode
    // todo оптимизировать частоту вызовов
    fun loadIfNeed(): Boolean {

        var noChanges = false
        if (isDevMode()) {
            if (SourceChangeDetector.checkChanges(Paths.get(builderDir!!, "src").toString())) {
                compile()
                load()
            }
        } else {
            val hasChanges = extractFromDb()
            if (hasDbSettings()) {
                if (hasChanges) {
                    load()
                } else {
                    noChanges = true
                }
            } else if (SourceChangeDetector.checkChanges(getRulesSettingsDir())) {
                load()
            }
        }
        if (!File(getRulesSettingsDir()).exists()) {
            throw Exception("Directory ${getRulesSettingsDir()} does not exist")
        }
        if (!noChanges && SourceChangeDetector.checkChanges(getProfileSettingsDir())) {
            ExchangeProfile.initialize()
            return true
        } else {
            return false
        }
    }

    var lastFileId: Any? = null

    private fun hasDbSettings(): Boolean {
        return lastFileId != null
    }

    private fun extractFromDb(): Boolean {
        val docs = ExchangeDatabase.configDb.getCollection(Names.Collections.settings).find().toList()
        var isNew = false
        if (docs.any()) {
            val doc = docs.first()
            val fileId = doc[Names.Fields.fileId]
            if (lastFileId != fileId) {
                isNew = true
                val gridFSBuckets = GridFSBuckets.create(ExchangeDatabase.configDb);
                val stream = gridFSBuckets.openDownloadStream(fileId as ObjectId)
                ExchangeZip.unzipFromStreamToFolder(stream, tempSettingsDir)
            }
            lastFileId = fileId
        } else {
            lastFileId = null
        }
        return isNew
    }

    private fun isDevMode(): Boolean {
        return !builderDir.isNullOrBlank()
    }

    private fun check() {
        var dir = File(getRulesSettingsDir())
        if (!dir.exists()) {
            throw Exception("Directory ${dir.absolutePath} does not exist")
        }

        if (isDevMode()) {
            dir = File(builderDir!!)
            if (!dir.exists()) {
                throw Exception("Directory ${dir.absolutePath} does not exist")
            }
        }
    }

    private fun compile() {
        Logger.traceFun {
            check()
            ExchangeSettingsCmd.compile()
        }
    }

    private fun load() {
        Logger.traceFun {
            check()
            flowsBySource.clear()
            flowsByTags.clear()
            flowsByName.clear()
            val path = Paths.get(getRulesSettingsDir(), "full.json").toUri()
            val file = File(path)
            loadDocument(parse(file.readText()))
        }
    }

    private var indexes: MutableList<Document>? = null
    fun createIndexes() {
        if (indexes != null) {
            indexes!!.forEach { colIndexes ->
                val colName = colIndexes["collection"].toString()
                (colIndexes["indexes"] as Iterable<Iterable<String>>).forEach { indexColumns ->
                    IndexManager.create(colName, false, indexColumns)
                }
            }
        }
    }

    private fun loadDocument(document: Document) {
        Logger.traceFun {
            (document["flows"] as MutableList<Document>).forEach {
                val rule = ExchangeRule.fromDocument(it)
                val flow = rule.operation[0]
                if (rule.enabled) {
//                    if (flow is ExchangeFlow && flow.output =="view_ObjectTree"){
//                        val a=1
//                    }
                    if (flow is ExchangeFlow && flow.operationType == ExchangeFlow.OperationType.view) {
                        views.add(flow)
                    } else {
                        if (flowsBySource[rule.trigger] == null) {
                            flowsBySource[rule.trigger!!] = mutableListOf()
                        }
                        flowsBySource[rule.trigger]!!.add(rule)

                        rule.tags.forEach {tag->
                            if (flowsByTags[tag] == null) {
                                flowsByTags[tag] = mutableListOf()
                            }
                            flowsByTags[tag]!!.add(rule)
                        }
                    }
                    if (rule.name!=null){
                        flowsByName[rule.name!!] = rule
                    }
                }

            }
            cascadeDeleteLinks.clear()

            (document["cascadeDeleteLinks"] as Iterable<String>).forEach {
                cascadeDeleteLinks.add(it)
            }

            //
            if (document["collectionIndexes"] != null) {
                indexes = document["collectionIndexes"] as MutableList<Document>
            }

            if (document["contextSettings"] != null) {
                contextSettings = document["contextSettings"] as MutableList<Document>
            }

            createIndexes()
            ViewManager.createViews()
        }
    }

    private fun parse(text: String): Document {
        return Logger.traceFun {
            return@traceFun Document.parse(text)
        }
    }

    private var contextSettings: MutableList<Document>? = null

    fun getContextSettings(): MutableList<Document>? {
        return contextSettings
    }
}