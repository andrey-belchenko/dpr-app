package cc.datafabric.adapter.lib.rdf

import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.common.XmlElement
import cc.datafabric.adapter.lib.data.DataDiff
import cc.datafabric.adapter.lib.data.IDataModel
import cc.datafabric.adapter.lib.general.IncomingOutMessage
import org.apache.jena.ontology.OntModel
import org.apache.jena.query.ARQ
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.util.iterator.NiceIterator
import org.bson.Document
import java.io.ByteArrayInputStream

object RdfFactory {
    init {
        //была ошибка в собранном jar, которая не воспроизводилась при дебаге
        //https://stackoverflow.com/questions/54905185/how-to-debug-nullpointerexception-at-apache-jena-queryexecutionfactory-during-cr
        ARQ.init()
    }

    private fun createCoreModel(): Model {
        return ModelFactory.createDefaultModel()
    }

    private fun createCoreOntologyModel(): OntModel {
        return ModelFactory.createOntologyModel()
    }

    fun createModel(): RdfModel {
        val model = createCoreModel()
        return RdfModel(model)
    }



    fun modelFromXml(value: String): IDataModel {
        val model = createCoreModel()
        ByteArrayInputStream(value.toByteArray()).use {
            RDFDataMgr.read(model, it, Lang.RDFXML)
        }
        return RdfModel(model)
    }

    fun modelFromJson(value: String): IDataModel {
        val model = createCoreModel()
        ByteArrayInputStream(value.toByteArray()).use{
            RDFDataMgr.read(model, it, Lang.JSONLD)
        }
        return RdfModel(model)
    }

    fun modelFromQuads(value: String): IDataModel {
        //todo эксперименты - убрать
        val dataSet = DatasetFactory.create()
        ByteArrayInputStream(value.toByteArray()).use{
            RDFDataMgr.read(dataSet, it, Lang.NQUADS)
        }
        val names = (dataSet.listModelNames() as NiceIterator).toList().map { it.uri }.toList()
        if (names.size!=1) {
            throw Exception("N-Quads data should contain one graph. Actual: ${names.joinToString()}")
        }
        val model= dataSet.getNamedModel(names.first())

        val ontModel = createCoreOntologyModel()

        ontModel.add(model)
        val typeList =  mutableListOf<String>()
        var classCount = 0
        var propCount = 0

        val classes = ontModel.listClasses().toList()


        classes.forEach {

            classCount++
//            if (it.localName=="ServiceLocation") {
//
//
//                x.listDeclaredProperties(false).forEach { pr ->
//                    val k = x
//                    val cc = pr
//
//
//
//                    if (cc.localName=="ServiceLocation.significance"){
//                        val bb = cc
//                    }
//                    if (cc.inverse!=null){
//                        val bb = cc
//                    }
//                }
//                val y = x
//            }
            val props = it.listDeclaredProperties(true)
            props.forEach { pr ->
                propCount++
//                if (pr.isLiteral){
//                    typeList.add(pr.rdfType.toString())
//                }



//                val k = x
//                val cc = pr
//                if (cc.localName=="Line.Towers"){
//                    val bb = cc
//                }
//                if (cc.inverse!=null){
//                    val bb = cc
//                }
            }
        }
        return RdfModel(ontModel)
    }


    fun modelFromMessages(messages:Iterable<IncomingOutMessage>): IDataModel {
        val doc = Document()
        val graph = arrayListOf<Document>()
        val context = Document()
        messages.forEach { message ->
            clearRedundantTypes(message)
            message.getPayloadDocuments().forEach { it ->
                graph.add(it)
            }
            //предполагается что префиксы во всех сообщениях одинаковые для одинаковых uri
            //todo вынести context в отдельную настройку, исключить из пайплайнов
            message.getContext().forEach {
                context[it.key] = it.value
            }
        }
        doc["@graph"] = graph
        doc["@context"] = context
        val json = BsonUtils.toJsonString(doc)
        return modelFromJson(json)
    }

    fun modelFromMessage(message: IncomingOutMessage): IDataModel {
        val doc= Document()
        clearRedundantTypes(message)
        doc["@graph"]=message.getPayload()
        doc["@context"]=message.getContext()
        val json= BsonUtils.toJsonString(doc)
        return modelFromJson(json)
    }

    //для корректного преобразования в дифф ссылка на объект не должна содержать тип иначе такой объект пересоздается
    private fun clearRedundantTypes(message: IncomingOutMessage){
        if (message.getVerb() =="delete") {
            return
        }
        message.getEntities().forEach {
            // todo хардкод, подумать над более корректной проверкой
            if (it.obj["astu:IdentifiedObject.name"]==null){
                it.obj.remove("@type")
            }
        }
    }

    fun diffFromXml(value: String): DataDiff {
        val root = XmlElement.parse(value)
        val buffer = XmlElement("buffer")
        buffer.moveChildrenFrom(root)
        return DataDiff(
            forwardDiffModel = createDiffPartModel(root, buffer, "forwardDifferences"),
            reverseDiffModel = createDiffPartModel(root, buffer, "reverseDifferences")
        )
    }

    fun diffFromXmlFullModel(value: String): DataDiff {
        val root = XmlElement.parse(value)
        root.removeChildren("FullModel")
        return DataDiff(
            forwardDiffModel = createModel(root)
        )
    }


    private fun createDiffPartModel(template: XmlElement, root: XmlElement, diffPartName: String): IDataModel? {
        val xmlDiffPart = root.descendants(diffPartName).firstOrNull()
        if (xmlDiffPart==null || !xmlDiffPart.children().any()){
            return null
        }
        val modelRoot = template.copy()
        modelRoot.moveChildrenFrom(xmlDiffPart)
        return createModel(modelRoot)
    }

    private fun createModel(modelXml: XmlElement): IDataModel? {
        val model = modelFromXml(modelXml.toXmlString())
        return model
    }
}