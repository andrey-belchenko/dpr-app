
import cc.datafabric.adapter.lib.exchange.ExchangeProfile
import cc.datafabric.adapter.lib.exchange.ExchangeProfileFilter
import org.bson.Document
import java.util.*
import javax.xml.namespace.QName
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

class XmlProcessor (val filter:ExchangeProfileFilter):IXmlProcessor {
    // todo проанализировать, сделать рефакторинг

    private  fun  getDocumentFromElement(element:XmlReader.ProcessorOpenElement):Document?{
        return element.customData as Document?
    }

    private fun clearFieldName(qname: QName):String {
        return qname.localPart.replace(".", "_")
    }

    private  fun  getEntityFromElement(element:XmlReader.ProcessorOpenElement):Document{
        return getDocumentFromElement(element)!!["entity"] as Document
    }

    private  fun  getLinksFromElement(element:XmlReader.ProcessorOpenElement):MutableList<Document>{
        return getDocumentFromElement(element)!!["links"] as MutableList<Document>
    }

    private  fun  getFieldsFromElement(element:XmlReader.ProcessorOpenElement):MutableList<Document>{
        return getDocumentFromElement(element)!!["fields"] as MutableList<Document>
    }

    private  fun clearId(value: String):String{
        // бывает _... и #_...
        var v  = value.replace("#","")
        if (v.startsWith("_")){
            v = v.removePrefix("_")
        }
        return v

    }

    private fun getEntityType (startElement:StartElement):String {
        var typeName = ""
        val elementName = startElement.name.localPart
        if (elementName!="Description"){
            typeName =  elementName
        }else{
            startElement.attributes.forEach {
                val attr = it as Attribute
                if (attr.name.localPart == "className") { // todo в стандарте этого поля нет, должно работать и без типа, доделать, проверить
                    typeName = attr.value
                }
            }
        }
        return  typeName
    }

    fun addAttrFromEntity (to:Document, from:Document) {
        to["itemTypeCode"] = from["entityType"]!!.toString() + "." + to["predicate"]!!.toString()
        to["entityId"] = from["entityId"]
        to["entityType"] = from["entityType"]
        to["action"] = from["action"]
    }
    override fun processElement(processorOpenElements: Stack<XmlReader.ProcessorOpenElement>) {

        val current = processorOpenElements.peek()
        val root = processorOpenElements[0]
        val elementName = root.startElement.name.localPart
        val typeName = getEntityType(root.startElement)
        var action = "create"
        if (elementName=="Description" || !filter.hasCreateClass(typeName)){
            action = "update"
        }

        if (current==root) {
            if (!filter.hasUpdateClass(typeName)) {
                return
            }

            val doc = Document(
                mapOf(
                    "entity" to Document(),
                    "links" to mutableListOf<Document>(),
                    "fields" to mutableListOf<Document>()
                )
            )
            root.customData = doc

            val entity = getEntityFromElement(root)
            var id: String? = null
            root.startElement.attributes.forEach {
                val attr = it as Attribute
                if (listOf("about", "ID").contains(attr.name.localPart)) {
                    id = attr.value
                }
            }
            entity["itemType"] = "entity"
            entity["itemTypeCode"] = typeName
            entity["entityId"] = clearId(id!!)
            entity["entityType"] = typeName
            entity["action"] = action
        }

        if (processorOpenElements.count()==2) {

            if (getDocumentFromElement(root)==null) {
                return
            }

            val rootEntity =  getEntityFromElement(root)
            var attr:Attribute? =  null
            current.startElement.attributes.forEach {
                val at = it as Attribute
                if (at.name.localPart == "resource"){
                    attr = at
                }
            }
            if (attr!=null){


                val propName = processorOpenElements.peek().startElement.name
                val propNameClear =  clearFieldName(propName)



                if (filter.hasProperty(typeName,propNameClear)){
                    val linkDocument = Document()

                    linkDocument["itemType"] = "link"
                    linkDocument["predicate"] = propNameClear
                    linkDocument["value"] = clearId(attr!!.value)
                    addAttrFromEntity(linkDocument, rootEntity)

                    val links = getLinksFromElement(root)
                    links.add(linkDocument)
                }

            }

        }
    }

    override fun processText(processorOpenElements: Stack<XmlReader.ProcessorOpenElement>, xmlEvent: XMLEvent) {


        if (processorOpenElements.count()==2) {
            val root = processorOpenElements[0]
            val typeName =  getEntityType(root.startElement)

            if (getDocumentFromElement(root)==null){
                return
            }

            val text = xmlEvent.asCharacters().data
//                if (text=="\$BaseVoltage10"){
//                    val a = 5
//                }
            if (text.trim().isBlank()){
                return
            }
            val rootEntity =  getEntityFromElement(root)
            val propName = processorOpenElements.peek().startElement.name
            val propNameClear = clearFieldName(propName)
            if (filter.hasProperty(typeName,propNameClear)){
                val propType = ExchangeProfile.getPropertyType(typeName,propNameClear)
                if (listOf("Asset","ConductingEquipment").contains(propType)){
                    val linkDocument = Document()

                    linkDocument["itemType"] = "link"
                    linkDocument["predicate"] = propNameClear
                    linkDocument["value"] = clearId( text)
                    addAttrFromEntity(linkDocument, rootEntity)

                    val links = getLinksFromElement(root)
                    links.add(linkDocument)
                }
                else {

                    val value = when (propType) {
                        "integer" -> text.toInt()
                        "boolean" -> text.toBoolean()
                        "date" -> text
                        "dateTime" -> text
                        "float" -> text.toFloat()
                        "string" -> text
                        "decimal" -> text.toBigDecimal()
                        //todo
                        "ActivePower" -> text.toBigDecimal()
                        "ReactivePower" -> text.toBigDecimal()
                        "Voltage" -> text.toBigDecimal()
                        "Seconds" -> text.toInt()
                        "CurrentFlow" -> text.toFloat()
                        "Susceptance" -> text.toFloat()
                        "Conductance" -> text.toFloat()
                        "PerCent" -> text.toBigDecimal()
                        "Resistance" -> text.toBigDecimal()
                        "Reactance" -> text.toBigDecimal()
                        "ApparentPower" -> text.toBigDecimal()
                        "KiloActivePower" -> text.toBigDecimal()
                        "Temperature" -> text.toBigDecimal()
                        "AssetInfo" -> text
                        //todo
                        "Asset" -> text
                        else -> {
                            throw NotImplementedError("Processing of type $propType not implemented value '$text'")
                        }
                    }
                    //todo есть еще такие свойства, они не обрабатываются сейчас
//                <cim:Location.mainAddress>
//                    <cim:StreetAddress>
//                    <cim:StreetAddress.streetDetail>
//                    <cim:StreetDetail>
//                    <cim:StreetDetail.addressGeneral>ул. 188</cim:StreetDetail.addressGeneral>
//                    </cim:StreetDetail>
//                    </cim:StreetAddress.streetDetail>
//                    </cim:StreetAddress>
//                </cim:Location.mainAddress>

                    val fieldDocument = Document()
                    fieldDocument["itemType"] = "field"
                    fieldDocument["predicate"] = propNameClear
                    fieldDocument["value"] = value


                    addAttrFromEntity(fieldDocument, rootEntity)


                    val fields = getFieldsFromElement(root)
                    fields.add(fieldDocument)
//                    rootEntity[clearFieldName(propName)] = value
                }
            }


        }
    }
}
