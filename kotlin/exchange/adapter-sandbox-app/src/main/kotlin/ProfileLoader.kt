import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.sys.Logger
import cc.datafabric.adapter.sandbox.app.MongoDbStore
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.bson.Document
import java.io.ByteArrayInputStream
import java.io.File

object ProfileLoader {



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

    fun loadProfile(){
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
        MongoDbStore.dropCollection("sys_ProfileClasses")
        MongoDbStore.dropCollection("sys_ProfileProps")

        Stopwatch.measure("enum"){
            classIris.forEach { classIri ->
                classCount++
                //без пересоздания модели с каждой итерацией работает все медленнее и медленнее невозможно дождаться
                //    ontModel.listClasses().forEach(){ cls->
                //        cls.listDeclaredProperties(false).forEach {prop->
                //        }
                //    }
                ontModel = ModelFactory.createOntologyModel()
                ontModel.add(model)
                val cls =  ontModel.getOntClass(classIri)

                val classDoc = Document()

                classDoc["name"] =  cls.localName
                val classProps = mutableListOf<Document>()
                classDoc["properties"] = classProps


                cls.listDeclaredProperties(false).forEachRemaining {prop->
                    val multiplicity = prop.asResource().listProperties()
                        .toList().find { it.predicate.localName == "multiplicity" }
                        ?.`object`
                        ?.asResource()?.uri?.split(":")
                        ?.last()
                    if (prop.localName=="EquipmentContainer.PlaceEquipmentContainer"){
                        val i =0
                    }
                    if (prop.localName=="IdentifiedObject.name"){
                        val i =0
                    }
                    propCount++
                    val isLiteral = literalTypes.contains(prop.range.localName)
                    val propDoc = Document()
                    propDoc["class"] =  cls.localName
                    propDoc["prop"] = prop.localName
                    propDoc["propType"] =  prop.range.localName
                    propDoc["propTypeNameSpace"] =  prop.range.nameSpace
                    if (multiplicity!=null){
                        propDoc["multiplicity"] =  multiplicity
                    }
                    propDoc["isLiteral"] = isLiteral

                    classProps.add(propDoc)
                    MongoDbStore.insert("sys_ProfileProps", propDoc)
                }
                MongoDbStore.insert("sys_ProfileClasses", classDoc)
                Logger.traceData("cls: $classCount , prp: $propCount")
            }
        }

        MongoDbStore.applyInsert()
        Logger.traceData(Stopwatch.toString())
    }

    fun toLoadFile(){

    }

}