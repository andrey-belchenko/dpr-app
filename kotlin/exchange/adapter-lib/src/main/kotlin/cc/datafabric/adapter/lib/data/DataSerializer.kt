package cc.datafabric.adapter.lib.data

import cc.datafabric.adapter.lib.common.XmlElement
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.time.LocalDateTime
import java.util.*

object DataSerializer {
    fun getDiffXml(diff: DataDiff):String{
        val modelId = UUID.randomUUID().toString()
        val createdAt = LocalDateTime.now().toString()

        val root = XmlElement.parse(
            """<rdf:RDF ${ConstNamespaces.rdf} ${ConstNamespaces.dm} ${ConstNamespaces.md} >
                   <dm:DifferenceModel rdf:about="#_$modelId">
                       <md:Model.created>$createdAt</md:Model.created>
                       <md:Model.version></md:Model.version>
                       <dm:forwardDifferences>
                       </dm:forwardDifferences>
                       <dm:reverseDifferences>
                       </dm:reverseDifferences>
                   </dm:DifferenceModel>
               </rdf:RDF>
            """.trimIndent()
        )

        root.descendants("forwardDifferences").first().add(
            getModelXmlForDiff(diff.getForwardDiffModel())
        )

        root.descendants("reverseDifferences").first().add(
            getModelXmlForDiff(diff.getReverseDiffModel())
        )
        return root.toXmlString()
    }

    private fun getModelXmlForDiff(model: IDataModel?):Iterable<XmlElement> {

        val list= mutableListOf<XmlElement>()
        if (model==null){
            return list
        }
        model.getEntities().forEach {
            list.add(getEntityXmlForDiff(it))
        }
        return list
    }

    private fun getEntityXmlForDiff(entity: IDataEntity): XmlElement {
        return Logger.traceFun {
            Logger.traceData(entity.getId())
            val el = if (!entity.isChanged()) {
                XmlElement(entity.getClass()!!.namespace, entity.getClass()!!.name)
            } else {
                XmlElement(ConstNamespaces.rdf, "Description")
            }
            el.setAttrValue(ConstNamespaces.rdf, "about", "#" + entity.getId())
            entity.getProperties().forEach {
                it.value.forEach {value->
                    el.add(getPropertyValueXmlForDiff(value))
                }

            }
            return@traceFun el
        }
    }

    private fun getPropertyValueXmlForDiff(propertyValue: IDataPropertyValue): XmlElement {
        val el = XmlElement(propertyValue.getNamespace(), propertyValue.getName())
        val text = propertyValue.getValueText()

        if (propertyValue.isReference()){
            el.setAttrValue(ConstNamespaces.rdf,"resource", "#$text")
        }else{
            el.setText(text)
        }
        return el
    }


    fun getModelJson(model: IDataModel?):Iterable<Document> {

        val list= mutableListOf<Document>()
        if (model==null){
            return list
        }
        model.getEntities().forEach {
            list.add(getEntityJson(it))
        }
        return list
    }


    private const val prefix = "astu:"
    private fun getEntityJson(entity: IDataEntity):Document {
        val el = Document()
        el["@id"] = prefix + entity.getId()
        if (entity.getClass()!=null){
            el["@type"] = prefix + entity.getClass()!!.name
        }

        entity.getProperties().forEach { prop ->
            val propPredicate =prefix+  prop.value.first().getName()
            if (prop.value.count() > 1) {
                val propValue = mutableListOf<Any>()
                prop.value.forEach { value ->
                    propValue.add(
                        getPropertyValueJson(value)
                    )
                }
                el[propPredicate] = propValue
            } else {
                el[propPredicate] = getPropertyValueJson(prop.value.first())
            }
        }
        return el
    }

    private fun getPropertyValueJson(propertyValue: IDataPropertyValue): Any {

        return if (propertyValue is IDataPropertyValueResource) {

            val doc = Document()
                .append("@id", prefix + propertyValue.getValueText())
            val cls = propertyValue.getValueClass()
            if (cls != null) {
                doc["@type"] = prefix + cls.name
            }
            doc
        } else {
            propertyValue.getValueText()
        }
    }
}