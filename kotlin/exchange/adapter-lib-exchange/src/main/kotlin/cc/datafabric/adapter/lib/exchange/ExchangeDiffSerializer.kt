package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.exchange.common.XmlElement
import cc.datafabric.adapter.lib.exchange.data.ConstNamespaces
import cc.datafabric.adapter.lib.exchange.data.Namespace
import org.bson.Document
import java.time.LocalDateTime
import java.util.*

object ExchangeDiffSerializer {

    private class ObjectInfo {
        var entity: Document? = null
        var properties: MutableList<Document> = mutableListOf()
    }

    //todo idPrefix - костыль
    fun getDiffXml(objects:Iterable<Document>, isSk:Boolean): String {

       val filteredObjects =  objects.filter { it["skip"]!=true }.toList()
       val idPrefix = if (isSk) "_" else ""
//        val objects = result.changedCollections.first().getDocuments().toList()
        // группировка по id объекта, удаленные отдельно
        val groupedMap = filteredObjects.groupBy { doc ->
            val itemInfo = doc["item"] as Document
            doc[Names.Fields.id].toString() + (itemInfo[Names.Fields.deletedAt] == null).toString()
        }
        val grouped = mutableListOf<ObjectInfo>()
        groupedMap.forEach { (_, documents) ->
            val objInfo = ObjectInfo()
            grouped.add(objInfo)
            documents.forEach {
                val itemType = it["itemType"] as String
                if (itemType == "entity") {
                    objInfo.entity = it
                } else {
                    objInfo.properties.add(it)
                }
            }
        }

        val modelId = UUID.randomUUID().toString()
        val modelCreatedAt = LocalDateTime.now().toString()
        var dmPrefix = ConstNamespaces.dm
        if (isSk){
            dmPrefix = ConstNamespaces.dmSk
        }
        val root = XmlElement.parse(
            """<rdf:RDF ${ConstNamespaces.rdf} ${dmPrefix.toString("dm")} ${ConstNamespaces.md} >
                   <dm:DifferenceModel rdf:about="#$idPrefix$modelId">
                       <md:Model.created>$modelCreatedAt</md:Model.created>
                       <md:Model.version></md:Model.version>
                       <dm:forwardDifferences>
                       </dm:forwardDifferences>
                       <dm:reverseDifferences>
                       </dm:reverseDifferences>
                   </dm:DifferenceModel>
               </rdf:RDF>
            """.trimIndent()
        )

        val forwardList = mutableListOf<XmlElement>()
        val reverseList = mutableListOf<XmlElement>()





//        val folderProp = DataProperty(ConstNamespaces.me, "IdentifiedObject.ParentObject", true)
//        val nameProp = DataProperty(ConstNamespaces.cim, "IdentifiedObject.name", false)
//        var folderCreated = false
//        forwardModel?.getEntities()?.toList()?.forEach { ent ->
//            if (!ent.isChanged() && !ent.getProperties().any { it.key == "IdentifiedObject.ParentObject" }) {
//                if (!folderCreated) {
//                    val folder = forwardModel.createEntity(folderUri, DataClass(ConstNamespaces.me, "Folder"))
//                    folder.addPropertyValue(nameProp, "Новые ТП-РП-ПС из САП")
//                    folderCreated = true
//                }
//                ent.addPropertyValue(folderProp, folderUri)
//            }
//        }


        var folderCreated = false
        grouped.forEach { objInfo ->
            val entInfo = objInfo.entity ?: objInfo.properties.first()!!
            val type = entInfo[Names.Fields.type].toString()
            val origId = entInfo[Names.Fields.id]
            val id = "$idPrefix$origId" // todo частный случай Ск-11
            val itemInfo = entInfo["item"] as Document


            val el = if (objInfo.entity != null) {
                val ns =  getClassNamespace(type,isSk)
                XmlElement(ns, type)

            } else {
                XmlElement(ConstNamespaces.rdf, "Description")
            }
            el.setAttrValue(ConstNamespaces.rdf, "about", "#" + id)

            var isCreate = false
            if (itemInfo[Names.Fields.deletedAt] == null) {
                if (objInfo.entity != null) {
                    isCreate = true
                }
                forwardList.add(el)
            } else {
                reverseList.add(el)
            }
//            var hasParent = false
            objInfo.properties.forEach { prop ->
                val origFieldName = prop[Names.Fields.predicate].toString()
//                if (origFieldName == "IdentifiedObject_ParentObject") {
//                    hasParent = true
//                }
                val fieldName = origFieldName.replace("_", ".")
                val ns = getPropertyNamespace(origFieldName, isSk)
                val propEl = XmlElement(ns, fieldName)
//                val text = prop[Names.Fields.value].toString()
                val text = BsonUtils.valueToString(prop[Names.Fields.value])
                val itemType = prop["itemType"] as String
                if (itemType=="link") {
                    propEl.setAttrValue(ConstNamespaces.rdf, "resource", "#$idPrefix$text")
                } else {
                    propEl.setText(text)
                }
                el.add(propEl)
            }

//            if (isSk && isCreate && !hasParent) {
//                val folderUri = "78440aed-08f6-44e7-872e-e6666730b1ba"
//                var origFieldName = "IdentifiedObject_ParentObject"
//                var fieldName = origFieldName.replace("_", ".")
//                var ns = getPropertyNamespace(origFieldName, isSk)
//                var propEl = XmlElement(ns, fieldName)
//                propEl.setAttrValue(ConstNamespaces.rdf, "resource", "#$idPrefix$folderUri")
//                el.add(propEl)
//                if (!folderCreated) {
//                    folderCreated = true
//                    val folder = XmlElement(ConstNamespaces.me, "Folder")
//                    folder.setAttrValue(ConstNamespaces.rdf, "about", "#$idPrefix$folderUri")
//                    origFieldName = "IdentifiedObject_name"
//                    fieldName = origFieldName.replace("_", ".")
//                    ns = getPropertyNamespace(origFieldName, isSk)
//                    propEl = XmlElement(ns, fieldName)
//                    propEl.setText("Новые ТП-РП-ПС из САП")
//                    folder.add(propEl)
//                    forwardList.add(folder)
//                }
//            }
        }
        root.descendants("forwardDifferences").first().add(forwardList)
        root.descendants("reverseDifferences").first().add(reverseList)
        return root.toXmlString()
    }

    //todo костыль
    private fun getPropertyNamespace(propertyName: String, isSk:Boolean): Namespace {
        if (!isSk){
            return  ConstNamespaces.cim
        }
//        if (propertyName == "IdentifiedObject_ParentObject") {
//            return ConstNamespaces.me
//        } else if (propertyName == "IdentifiedObject_childObjects"){
//            return ConstNamespaces.me
//        } else if (listOf("MonitelDiagram_PSRs","PowerSystemResource_Diagrams").contains(propertyName)){
//            return ConstNamespaces.me
//        } else if (propertyName == "IdentifiedObject_OrganisationRoles"){
//            return ConstNamespaces.rf
//        }
        return ExchangeProfile.getPropertyNamespace(propertyName)
    }

    private fun getClassNamespace(className: String, isSk:Boolean): Namespace {
        if (!isSk){
            return  ConstNamespaces.cim
        }
        return ExchangeProfile.getClassNamespace(className)
    }
}