package cc.datafabric.extensions

import org.bson.Document

object BsonPath {
    fun evaluate(document: Document, expression: String):Iterable<String> {
        val list = mutableListOf<String>()
        if (!expression.startsWith("$")) {
            list.add(expression)
        } else {
            var current = listOf(document)
            val pathList = expression.split(".")
            var isLast: Boolean
            var index = 1
            pathList.forEach { fieldExpr ->
                isLast = index == pathList.count()
                val fieldName = fieldExpr.replace("$", "")
                if (!isLast){
                    val next = mutableListOf<Document>()
                    current.forEach { document ->
                        getValueIfDocument(document, fieldName).forEach {
                            next.add(it)
                        }
                    }
                    current = next
                }else{
                    current.forEach { document ->
                        getValueIfString(document, fieldName).forEach {
                            list.add(it)
                        }
                    }
                }
                index++
            }
        }
        return list
    }

    private fun getValueIfDocument(document: Document, fieldName: String):Iterable<Document>{
        val list = mutableListOf<Document>()
        when (val value = document[fieldName]) {
            is Document -> list.add(value)
            is List<*> -> value.forEach { doc ->
                if (doc is Document) {
                    list.add(doc)
                }
            }
        }
        return list
    }
    private fun getValueIfString(document: Document, fieldName: String):Iterable<String>{
        val list = mutableListOf<String>()
        when (val value = document[fieldName]) {
            is String -> list.add(value)
            is List<*> -> value.forEach { str ->
                if (str is String) {
                    list.add(str)
                }
            }
        }
        return list
    }
}