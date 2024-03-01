package cc.datafabric.adapter.sandbox.app

import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.sys.Logger
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.bson.Document
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.StartElement

object Tools {


    class  XmlReader (private val reader:XMLEventReader) {

        class ProcessorOpenElement (val startElement:StartElement){
            var customData:Any? = null
        }


        private  val openElements = Stack<StartElement>()
        private  val processorOpenElements = Stack<ProcessorOpenElement>()
        private  var lastProcessorElement:ProcessorOpenElement? = null

        fun descendant(tag: String): XmlReader{
            return descendant(tag,false)
        }

        fun child(tag: String): XmlReader{
            return descendant(tag,true)
        }

        fun child(): XmlReader{
            return descendant(null,true)
        }

        private fun descendant(tag: String?, isImmediate: Boolean): XmlReader {
            val initialDepth = openElements.count()
            while (!isEnd) {
                next()
                if (tag==null || getCurrentTag() == tag) {
                    if (!isImmediate || openElements.count() == initialDepth + 1) {
                        break
                    }
                }
            }
            return this
        }

        fun following(): XmlReader {
            val initialDepth = openElements.count()
            while (!isEnd) {
                next()
                if (openElements.count() == initialDepth) {
                    break
                }
            }
            return this
        }

        var isEnd:Boolean = false
        private fun next(): XmlReader {
            val startElement: StartElement?
            while (reader.hasNext()) {

                val event = reader.nextEvent()
                if (event.isStartElement) {

                    startElement = event.asStartElement()
                    openElements.push(startElement)
                    processElement()
                    break
                } else if (event.isEndElement) {
                    endProcessElement()
                    openElements.pop()
                } else if (event.isCharacters){
                    val text = event.asCharacters().data
                    processText(text)

                }
            }
            isEnd = !reader.hasNext()
            return this
        }


        private fun processElement() {
            if (processor == null) return
            processorOpenElements.push(ProcessorOpenElement(openElements.peek()))
            processor?.processElement(processorOpenElements)
        }

        private fun processText(text: String) {
            if (processor == null) return
            processor?.processText(processorOpenElements,text)
        }

        private fun endProcessElement() {
            if (processor == null) return

            if (processorOpenElements.empty()){
                lastProcessorElement = null
                return
            }
            lastProcessorElement =  processorOpenElements.pop()
        }

        private fun getCurrentElement():StartElement?{
            if (!openElements.any()){
                return  null
            }
            return openElements.peek()
        }
        private fun getCurrentTag():String? {
           return getCurrentElement()?.name?.localPart
        }


        private fun setProcessor(processor: IXmlProcessor){
            clearProcessor()
            this.processor = processor
            processElement()


        }

        private fun clearProcessor(){
            this.processor = null
            processorOpenElements.clear()
        }
        fun readElement(processor: IXmlProcessor):Any? {
            setProcessor(processor)
            this.processor = processor
            following()

            val value = this.lastProcessorElement?.customData
            clearProcessor()
            return value
        }

        private var processor:IXmlProcessor? = null
    }

    interface IXmlProcessor {
        fun processElement(processorOpenElements:Stack<XmlReader.ProcessorOpenElement>)
        fun processText(processorOpenElements:Stack<XmlReader.ProcessorOpenElement>, text: String)
    }

    class XmlProcessor (val clearFieldNames:Boolean):IXmlProcessor {


        private  fun  getDocumentFromElement(element:XmlReader.ProcessorOpenElement):Document?{
            return element.customData as Document?
        }

        private fun clearFieldName(qname: QName):String{
            if (!clearFieldNames){
                return qname.localPart
            }
            return  qname.localPart.replace(".","_")
        }

        private  fun  getEntityFromElement(element:XmlReader.ProcessorOpenElement):Document{
            return getDocumentFromElement(element)!!["entity"] as Document
        }

        private  fun  getLinksFromElement(element:XmlReader.ProcessorOpenElement):MutableList<Document>{
            return getDocumentFromElement(element)!!["links"] as MutableList<Document>
        }

        private  fun clearId(value: String):String{
            // бывает _... и #_...
            var v  = value.replace("#","")
            if (v.startsWith("_")){
                v = v.removePrefix("_")
            }
            return v

        }
        override fun processElement(processorOpenElements: Stack<XmlReader.ProcessorOpenElement>) {

            val current = processorOpenElements.peek()
            val root = processorOpenElements[0]
            val typeName = root.startElement.name.localPart
            if (current==root) {
                if (!Profile.hasClass(typeName)) {
                    return
                }
                root.customData = Document(
                    mapOf(
                        "entity" to Document("model", Document()),
                        "links" to mutableListOf<Document>()
                    )
                )

                val rootEntity = getEntityFromElement(root)
                var id: String? = null
                root.startElement.attributes.forEach {
                    val attr = it as Attribute
                    if (listOf("about", "ID").contains(attr.name.localPart)) {
                        id = attr.value
                    }
                }
                rootEntity["@id"] = clearId(id!!)
                rootEntity["@type"] = typeName
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

                    val propName = current.startElement.name
                    if (Profile.hasProperty(typeName,propName.localPart)){
                        val linkDocument = Document()
                        linkDocument["entity"] = rootEntity["@id"]!!
                        linkDocument["name"] = propName.localPart
                        linkDocument["value"] = clearId(attr!!.value)
                        val links = getLinksFromElement(root)
                        links.add(linkDocument)
                    }

                }

            }
        }

        override fun processText(processorOpenElements: Stack<XmlReader.ProcessorOpenElement>, text: String) {
            if ( text.trim().isBlank()){
                return
            }
            if (processorOpenElements.count()==2) {
                val root = processorOpenElements[0]
                val typeName = root.startElement.name.localPart
                if (getDocumentFromElement(root)==null){
                    return
                }

//                if (text=="\$BaseVoltage10"){
//                    val a = 5
//                }

                val rootEntity =  getEntityFromElement(root)
                val propName = processorOpenElements.peek().startElement.name
                if (Profile.hasProperty(typeName,propName.localPart)){
                    val propType = Profile.getPropertyType(typeName,propName.localPart)

                    if (listOf("Asset","ConductingEquipment").contains(propType)){
                        val linkDocument = Document()
                        linkDocument["entity"] = rootEntity["@id"]!!
                        linkDocument["name"] = propName.localPart
                        linkDocument["value"] = clearId( text)
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
                        (rootEntity["model"] as Document)[clearFieldName(propName)] = value
                    }
                }


            }
        }
    }

}