import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.io.InputStream
import javax.xml.stream.XMLInputFactory

object Loader {

    private const val bufferCollection = "sys_DiffBuffer"
    fun load(data: InputStream, filterName: String, isImport: Boolean) {


        ExchangeSettingsRepository.loadIfNeed()
        ExchangeStore.inProgress = true
        ExchangeProfile.initialize() //todo пока тут, сделать чтобы читалось один раз в режиме релиза
        ExchangeDatabase.dropCollection(bufferCollection)
        loadModel(data, true, filterName, isImport)
        data.reset()
        loadModel(data, false, filterName, isImport)
        ExchangeStore.applyInsert()
        val pipeline = listOf(
            Document(
                "\$unset", "_id"
            ),
            Document(
                "\$merge",
                Document(
                    mapOf(
                        "into" to Names.Collections.modelImport,
                    )
                )
            )
        )
        ExchangeDatabase.executeAggregateNonQuery(bufferCollection, pipeline)
        ExchangeDatabase.dropCollection(bufferCollection)
        CollectionChangeInfoStore.set(Names.Collections.modelImport)
        CollectionChangeInfoStore.commit()

        ExchangeStore.inProgress = false
    }

    private fun loadModel(inputStream: InputStream, isForward: Boolean, filterName: String, isImport: Boolean) {
        val collectionName = bufferCollection
        val xmlInputFactory = XMLInputFactory.newInstance()
        val eventReader =
            xmlInputFactory.createXMLEventReader(inputStream)
        val reader = XmlReader(eventReader)

        reader.goToChild("RDF").goToChild(listOf("DifferenceModel", "FullModel"))
        if (reader.isEnd) return

        if (reader.getCurrentTag() == "FullModel") {
            if (!isForward) return
            reader.goToFollowing()
        } else {
            val modelTag = if (isForward) "forwardDifferences" else "reverseDifferences"
            reader.goToChild(modelTag)
            if (reader.isEnd) return
            reader.goToChild()
        }
        if (reader.isEnd) return

        var allCount = 0L
        var acceptedCount = 0L
        val processor = XmlProcessor(ExchangeProfileFilter(filterName))
        val portion = 10000
        var portionCount = 0


//        val changedAt = ExchangeTimestamp.now()
        while (reader.hasCurrent()) {
            val objData = reader.readElement(processor) as Document?
            if (objData != null) {

                val doc = Document()
                if (!filterName.isNullOrBlank()) {
                    doc[Names.Fields.filterName] = filterName
                }
//                doc["changedAt"] = changedAt
                val ent = objData["entity"] as Document
                var action = ent["action"].toString()
                var linkAction = "link"
                if (!isForward) {
                    action = when (action) {
                        "create" -> "delete"
                        "update" -> "link"
                        else -> throw NotImplementedError("action:$action")
                    }
                    linkAction = "deleteLink"
                }

                val order = when (action) {
                    "create" -> 1
                    "update" -> 2
                    "link" -> 2
                    "delete" -> 2
                    else -> throw NotImplementedError("action:$action")
                }
                doc["order"] = order
                val model = Document(
                    mapOf(
                        Names.Fields.atType to ent["entityType"],
                        Names.Fields.atId to ent["entityId"].toString().lowercase(),
                        Names.Fields.atAction to action,
                        Names.Fields.atIdSource to "platform",
                        Names.Fields.atAttr to Document(
                            mapOf(
                                "skLoadedAt" to BsonUtils.getCurrentTimeStamp(),

                                )
                        ),
                        Names.Fields.atLastSource to "sk11"
                    )
                )
                doc[Names.Fields.model] = model
                val fields = objData["fields"] as Iterable<Document>
                fields.forEach {
                    model[it["predicate"].toString()] = it["value"]
                }

                val links = objData["links"] as Iterable<Document>
                links.forEach {
                    val fieldName = it["predicate"].toString()
                    var list: MutableList<Document>? = null
                    if (model[fieldName] != null) {
                        if (model[fieldName] is Document) {
                            val buffer = model[fieldName] as Document
                            list = mutableListOf()
                            list.add(buffer)
                            model[fieldName] = list
                        } else {
                            list = model[fieldName] as MutableList<Document>?
                        }
                    }
                    val item = Document(
                        mapOf(
                            Names.Fields.atId to it["value"].toString().lowercase(),
                            Names.Fields.atAction to linkAction,
                            Names.Fields.atIdSource to "platform",
                            Names.Fields.atAttr to Document(
                                mapOf(
                                    "skLoadedAt" to BsonUtils.getCurrentTimeStamp()
                                )
                            ),
                            Names.Fields.atLastSource to "sk11"
                        )
                    )
                    if (list != null) {
                        list.add(item)
                    } else {
                        model[fieldName] = item
                    }
                }
                ExchangeStore.put(collectionName, doc)
                acceptedCount++
            }
            allCount++
            portionCount++
            if (portionCount == portion) {
                portionCount = 0
                if (isForward) { // todo: сделать подсчет общего к-ва
                    progressAlert(allCount, acceptedCount)
                }

            }
            reader.goToFollowing()
        }
        if (isForward) { // todo: сделать подсчет общего к-ва
            progressAlert(allCount, acceptedCount)
        }


    }

    private fun progressAlert(allCount: Long, acceptedCount: Long) {
        val message = "all count:$allCount, accepted count:$acceptedCount"
        Logger.traceData("all count:$allCount")
        ExchangeStatusStore.setProcessorProgress(message)
    }
}