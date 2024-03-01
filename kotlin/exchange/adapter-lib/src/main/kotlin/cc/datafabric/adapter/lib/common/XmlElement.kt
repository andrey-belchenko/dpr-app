package cc.datafabric.adapter.lib.common


import cc.datafabric.adapter.lib.data.Namespace
import org.joox.JOOX
import org.joox.Match
import org.xml.sax.InputSource
import java.io.StringReader
import cc.datafabric.adapter.lib.sys.*

//Не нашел удобного способа работать с XML в java (чтение/запись)
//https://www.baeldung.com/java-modify-xml-attribute
//Нет уверености, что JOOX хороший вариант, поэтому сделана обертка, при необходимости можно заменить на что-нибудь другое
//Производительность решения на больших объемах под вопросом
//В старом варианте парсинг XML выполнялся поиском тегов по строке
class XmlElement {
    companion object {
        private val builder = JOOX.builder()
        fun parse(xmlText: String): XmlElement {

            return XmlElement(parseMatch(xmlText))
        }

        private fun parseMatch(xmlText: String): Match{
            val source = InputSource(StringReader(xmlText))
            val input = builder.parse(source)

            return JOOX.`$`(input)
        }


    }

    private var match: Match? = null

    constructor(namespace: Namespace, tagName: String) {
        val text =
            if (namespace.prefix == "") {
                "<$tagName $namespace/>"
            } else {
                "<${namespace.prefix}:$tagName $namespace/>"
            }

        match = parseMatch(text)
    }

    constructor(tagName: String) {
        val text = "<$tagName/>"
        match = parseMatch(text)
    }

//    constructor(element: XmlElement):this(element.toXmlString())
//    {
//        match = element.match!!.copy() не копирует содержимое только ссылку
//    }

    private constructor(match: Match) {
        this.match = match
    }

    fun add(element: XmlElement) {
        match!!.append(element.match)
    }

    fun add(elements: Iterable<XmlElement>) {
        elements.forEach {
            match!!.append(it.match)
        }
    }

    fun getText(): String {
        return match!!.text()
    }

    fun getTagName(): String {
        return match!!.tag()
    }
    fun setText(text: String?) {
         match!!.text(text)
    }

    fun setCdata(text: String) {
        match!!.cdata(text)
    }

    private fun normalizeNamespaces(){
        //объявление всех namespace переносится в root, чтобы они не дублировались в дочерних элементах при сериализации в строку
        val prefMap = mutableMapOf<Int, String>()
        match!!.xpath(".//*").namespacePrefixes().forEachIndexed { i, it ->
            if (it != null) {
                prefMap[i] = it
            }
        }
        val iriMap = mutableMapOf<Int, String>()
        match!!.xpath(".//*").namespaceURIs().forEachIndexed { i, it ->
            if (it != null) {
                iriMap[i] = it
            }
        }
        val nsMap = mutableMapOf<String, String>()
        prefMap.forEach() {
            val oldIri = nsMap[it.value]
            val newIri = iriMap[it.key]!!
            if (oldIri==null){
                nsMap[it.value] = newIri
            }else if (oldIri!=newIri) {
                Logger.traceData(match!!.toString())
                throw  Exception("Can't use same prefix for different namespaces ${it.value}: $oldIri, $newIri")
            }
        }
        nsMap.forEach(){
            match!!.attr("xmlns:${it.key}",it.value)
        }
    }

    fun setAttrValue(namespace: Namespace, name: String, value: String){
        match!!.attr("${namespace.prefix}:$name",value)
    }

    fun getAttrValue(namespace: Namespace, name: String): String{
       return  match!!.attr("${namespace.prefix}:$name")
    }


    fun toXmlString(): String {
        normalizeNamespaces()
        return match!!.toString()
    }

    fun copy(): XmlElement {
        return parse(this.toXmlString())
    }

    fun moveChildrenFrom(source: XmlElement) {
        val children = source.match!!.children()
        this.match!!.append(children) // todo не понятно перемещаются или копируются вроде по разному бывает, понять от чего зависит
        source.match!!.children().remove()
    }

    fun removeChildren(name: String) {
        match!!.children(name).remove()
    }

    fun removeChildren() {
        match!!.children().remove()
    }
    fun descendants(namespaceUri:String?, name: String): Sequence<XmlElement> = sequence {
        val found = if (namespaceUri == null) {
            match!!.xpath(".//$name").get()
        } else {
            match!!.namespace("ns", namespaceUri).xpath(".//ns:$name").get()
        }
        found.forEach {
            yield(XmlElement(JOOX.`$`(it)))
        }
    }
    fun descendants(name: String): Sequence<XmlElement> = sequence {
        val found =  match!!.find(name) // возвращает по имени не зависимо от namespace
        found.forEach {
            yield(XmlElement(JOOX.`$`(it)))
        }
    }

    fun children(namespaceUri:String?,name: String): Sequence<XmlElement> = sequence {
        val found = if (namespaceUri == null) {
            match!!.xpath("./$name").get()
        } else {
            match!!.namespace("ns", namespaceUri).xpath("./ns:$name").get()
        }
        found.forEach {
            yield(XmlElement(JOOX.`$`(it)))
        }
    }
    fun children(name: String): Sequence<XmlElement> = sequence {
        val found = match!!.children(name) // возвращает по имени не зависимо от namespace
        found.forEach {
            yield(XmlElement(JOOX.`$`(it)))
        }
    }

    fun children(): Sequence<XmlElement> = sequence {
        val found = match!!.children()
        found.forEach {
            yield(XmlElement(JOOX.`$`(it)))
        }
    }


    operator fun get(name: String): XmlElement? {
        return children(name).firstOrNull()
    }
}