import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.sys.Logger
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.bson.Document
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
// todo большая часть скопирована из ProfileLoader
object ProfileLoaderCsv {



    //todo наверное есть способ определить свойства литералы но я его не нашел
    private  val literalTypes = listOf(
        "integer",
        "boolean",
        "date",
        "dateTime",
        "float",
        "string",
        "decimal",
        //todo
        "ActivePower",
        "ReactivePower",
        "Voltage",
        "Seconds",
        "CurrentFlow",
        "Susceptance",
        "Conductance",
        "PerCent",
        "Resistance",
        "Reactance",
        "ApparentPower",
        )

    fun exportToCsv(){

//        val profileText= File("C:\\Repos\\datafabric\\adapter-lite\\adapter-sandbox-app\\testData\\profile-siber-sk.xml").readText()
        val profileText= File("C:\\Repos\\datafabric\\adapter-lite\\adapter-sandbox-app\\testData\\profile.xml").readText()
        val dataSet = DatasetFactory.create()
        ByteArrayInputStream(profileText.toByteArray()).use{
            RDFDataMgr.read(dataSet, it, Lang.NQUADS)
        }
        val model= dataSet.getNamedModel(dataSet.listModelNames().next())
        var ontModel = ModelFactory.createOntologyModel()
        ontModel.add(model)
        val classIris = ontModel.listClasses().toList().map { it.uri }
        var classCount =  0
        var propCount = 0

        val csvPath: Path = Paths.get( "C:\\Repos\\datafabric\\mrsk-configuration\\exchange-configuration\\settings\\profile\\profile.csv")
        Files.deleteIfExists(csvPath)
        val csvPrinter = CSVPrinter(Files.newBufferedWriter(csvPath), CSVFormat.DEFAULT)


        csvPrinter.printRecord("className","predicate","range","multiplicity","isLiteral","inverseOf")
        Stopwatch.measure("enum"){
            classIris.sortedBy { it }.toList().forEach { classIri ->
                classCount++
                //без пересоздания модели с каждой итерацией работает все медленнее и медленнее невозможно дождаться
                //    ontModel.listClasses().forEach(){ cls->
                //        cls.listDeclaredProperties(false).forEach {prop->
                //        }
                //    }
                if (classCount<10000){
                    ontModel = ModelFactory.createOntologyModel()
                    ontModel.add(model)
                    val cls =  ontModel.getOntClass(classIri)

                    val classDoc = Document()

                    classDoc["name"] =  cls.localName
                    val classProps = mutableListOf<Document>()
                    classDoc["properties"] = classProps


                    cls.listDeclaredProperties(false).toList().sortedBy { it.localName }.toList().forEach {prop->
                        val multiplicity = prop.asResource().listProperties()
                            .toList().find { it.predicate.localName == "multiplicity" }
                            ?.`object`
                            ?.asResource()?.uri?.split(":")
                            ?.last()
                        propCount++
                        val isLiteral = literalTypes.contains(prop.range.localName)
                        csvPrinter.printRecord(
                            cls.localName,
                            prop.localName,
                            prop.range.localName,
                            multiplicity,
                            isLiteral,
                            prop.inverseOf
                        )
                    }
                    Logger.traceData("cls: $classCount , prp: $propCount")
                }

            }

        }
        csvPrinter.flush()
        csvPrinter.close()
        Logger.traceData(Stopwatch.toString())
    }


}