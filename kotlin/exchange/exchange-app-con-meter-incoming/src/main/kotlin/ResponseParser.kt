package cc.datafabric.exchange.app.con.meter.incoming

import org.bson.Document
import org.joox.JOOX.`$`
import org.joox.Match

object ResponseParser {
    fun parse(value: String, tagName:String): Iterable<Document> {
        val docs = mutableListOf<Document>()
        val xmlDoc = `$`(value)
        val usagePoints = xmlDoc
            .find("Payload")
            .children()
            .children(tagName)
        usagePoints.forEach { element ->
            val children = `$`(element).children()
            val doc = documentFromChildren(children)
            docs.add(doc)
        }
        return docs
    }

    private fun documentFromChildren(children: Match): Document {
        val doc = Document()
        children.forEach { childElement ->
            val nestedChildren = `$`(childElement).children()
            val value: Any?
            var propName = childElement.localName
            if (propName == "customAttribute") {
                propName = `$`(childElement).children("name")[0].textContent.trim()
                value = `$`(childElement).children("value")[0].textContent.trim()
            } else if (nestedChildren.any()) {
                value = documentFromChildren(nestedChildren)
            } else {
                var strVal: String? = childElement.textContent.trim()
                if (strVal == "") {
                    strVal = null
                }
                value = strVal
            }
            if (value!= null){
                doc[propName] = value
            }
        }
        return doc
    }
}