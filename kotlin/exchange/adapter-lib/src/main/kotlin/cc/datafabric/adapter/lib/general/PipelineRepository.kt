package cc.datafabric.adapter.lib.general

import org.bson.Document
import java.io.File
import java.nio.file.Paths
import cc.datafabric.adapter.lib.sys.*
import java.util.concurrent.ConcurrentHashMap

object PipelineRepository {
    private val cache= ConcurrentHashMap<String,MutableList<Document>>()
    fun get(name:String):MutableList<Document>{
        if (!cache.containsKey(name)){
            val dir = Config.get("adpPipelinesDir")
            Logger.traceBeg("reading pipeline text for '$name' from '$dir'")
            val path = Paths.get(dir,"$name.js")
            val text = File(path.toString()).readText(
                Charsets.UTF_8
            )
            Logger.traceData(text)

            Logger.traceBeg("parsing pipeline")
            val pipeline = Document.parse(" {'json': $text } ")["json"] as MutableList<Document>
            pipeline.add(0, Document()) //в будет заменяться на входящее сообщение
            cache[name]=pipeline
        }
        return  cache[name]!!
    }
}