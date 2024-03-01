package cc.datafabric.adapter.sandbox.app

import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.exchange.ExchangeTimestamp
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.io.FileInputStream
import javax.xml.stream.XMLInputFactory

object MongoDbLoader {
    fun main(loadId: String) {
        Profile.initialize()
//https://www.baeldung.com/java-stax
        val xmlInputFactory = XMLInputFactory.newInstance()
        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\Воронежэнерго_2022_11_23.xml"))
        val reader = Tools.XmlReader(eventReader)


        reader.child("RDF").child("FullModel")
        reader.following()
        var allCount=0
        var acceptedCount=0
        val processor = Tools.XmlProcessor(true)
        val portion = 100000
        var portionCount = 0
        MongoDbStore.dropCollection("sys_model_Input")
//        MongoDbStore.dropCollection("load_Link")
        val changedAt = ExchangeTimestamp.now()
        Stopwatch.measure("all") {
            while (!reader.isEnd) {
                val objData = reader.readElement(processor) as Document?
                if (objData != null) {
                    Stopwatch.measure("insert entities") {
                        val doc = Document()
                        doc["changedAt"] = changedAt
                        val ent = objData["entity"] as Document
                        val model = ent["model"] as Document
                        doc["isSource"] = "platform"
                        doc["batchId"] = loadId
                        doc["operationId"] = loadId
                        doc["model"] = model
                        model["@id"] = ent["@id"]
                        model["@type"] = ent["@type"]
                        model["@action"] = "create"


                        val links = objData["links"] as Iterable<Document>
                        links.forEach {
                           val name = it["name"].toString().replace(".","_")
                            val value = Document(
                                mapOf(
                                    "@id" to it["value"]
                                )
                            )
                            model[name] = value
//                            MongoDbStore.insert("load_Link", it)

                        }

                        MongoDbStore.insert("sys_model_Input", doc)
                    }

//                    Stopwatch.measure("insert entities") {
//                        MongoDbStore.insert("load_Entity", objData["entity"] as Document)
//                    }


//                    val links = objData["links"] as Iterable<Document>
//                    links.forEach {
//                        Stopwatch.measure("insert links") {
//                            MongoDbStore.insert("load_Link", it)
//                        }
//                    }

                    acceptedCount++

                }
//                val ss =  BsonUtils.toJsonString(objData as Document)
                allCount++
                portionCount++
                if (portionCount == portion) {
                    portionCount = 0
                    Logger.traceData("all count:$allCount")
                    Logger.traceData("accepted count:$acceptedCount")
                }
//                if (allCount == 100000) {
//                    break
//                }
            }
        }
        Stopwatch.measure("applyInsert") {
            MongoDbStore.applyInsert()
        }
        Logger.traceData(Stopwatch.toString())
        Logger.traceData("all count:$allCount")
        Logger.traceData("accepted count:$acceptedCount")
    }
}