package cc.datafabric.adapter.sandbox.app

import LoaderCsvFiles
import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import org.bson.json.JsonWriterSettings
import java.io.FileInputStream
import javax.xml.stream.XMLInputFactory

object CockroachLoader {

    fun profile(){
        Profile.initialize()

        Profile.profileMap.forEach {clsRec->
            clsRec.value.forEach{propRec->
                val obj= propRec.value
                Stopwatch.measure("csv"){
                    LoaderCsvFiles.profileCsvPrinter.printRecord(
                        obj["class"],
                        obj["prop"].toString().split('.').first(),
                        obj["prop"],
                        obj["propType"]
                    )
                }

            }
        }

        LoaderCsvFiles.close()

        Logger.traceData(Stopwatch.toString())

        CockroachUserfile.uploadUserfileToCockroachDb(LoaderCsvFiles.profileFilePath.toAbsolutePath())



        CockroachStore.initProfileStagingTable()


        CockroachStore.importProlile()


    }

    fun main() {


        Profile.initialize()
//https://www.baeldung.com/java-stax
        val xmlInputFactory = XMLInputFactory.newInstance()
        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\Воронежэнерго_2022_11_23.xml"))
//        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\Полная модель Россети Сибирь.xml"))
//        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\KOTMI Rdf.xml"))
//        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\ПС Сибирь.xml"))
//        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\Сибирь.xml"))
//        val eventReader = xmlInputFactory.createXMLEventReader(FileInputStream("C:\\Bin\\Сибирь ПС ГОСТ.3.xml"))

        val reader = Tools.XmlReader(eventReader)


        reader.child("RDF").child("FullModel").following() //SK

//        reader.child("RDF").child() // kotmi


        var allCount=0
        var acceptedCount=0
        val processor = Tools.XmlProcessor(false)
        val portion = 100000
        var portionCount = 0
        CockroachStore.initTables()

        Stopwatch.measure("all") {
            while (!reader.isEnd) {
                val objData = reader.readElement(processor) as Document?
                if (objData != null) {
                    val objEntity =  objData["entity"] as Document

                    val model = (objEntity["model"]  as Document).toJson(
                        JsonWriterSettings
                            .builder()
                            .indent(false)
                            .build())
                    LoaderCsvFiles.entitiesCsvPrinter.printRecord(
                        objEntity["@id"],
                        objEntity["@type"],
                        model
                    )


                    val links = objData["links"] as Iterable<Document>
                    links.forEach {objLink->
                        LoaderCsvFiles.linksCsvPrinter.printRecord(
                            objLink["entity"],
                            objLink["name"],
                            objLink["value"]
                        )
                    }
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
//                if (allCount == 10000) {
//                    break
//                }
            }
        }
//        MongoDbStore.applyInsert()
        Logger.traceData(Stopwatch.toString())
        Logger.traceData("all count:$allCount")
        Logger.traceData("accepted count:$acceptedCount")

        LoaderCsvFiles.close()

        Logger.trace("upload files"){
            CockroachUserfile.uploadUserfileToCockroachDb(LoaderCsvFiles.entitiesFilePath.toAbsolutePath())
            CockroachUserfile.uploadUserfileToCockroachDb(LoaderCsvFiles.linksFilePath.toAbsolutePath())
        }
        Logger.trace("import files"){
            CockroachStore.importEntities()
            CockroachStore.importLinks()
        }

    }
}