import cc.datafabric.adapter.lib.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString


object PipelineConverter {
    val sourceFolder = "C:\\Repos\\datafabric\\adapter-lite\\adapter-app-rabbit-incoming\\pipelines"
    val targetFolder = "C:\\Repos\\datafabric\\mrsk-configuration\\exchange-configuration\\builder\\src\\rules\\in\\other"
    fun convertAll() {

        val sbAllImport = StringBuilder()
        val sbAllUse = StringBuilder()
//        sbAllImport.appendLine("//#region imports")
        File(sourceFolder).listFiles()?.forEach {
            if (!it.isDirectory) {
                val text = it.readText()
                Logger.traceData(it.name)
                val name = it.name.replace(".js","")
                sbAllImport.appendLine("import * as $name from './$name'")
                sbAllUse.appendLine("    $name.rule,")
                val doc = convertPipeline(it.name,text)
                writePipeline(it.name, doc)
            }
        }
//        sbAllImport.appendLine("//#endregion")
        val allText = """
$sbAllImport

export var rules = [
$sbAllUse
]            
        """.trimIndent()
        val path  = Paths.get(targetFolder,"_all.ts")
        val file  = File(path.absolutePathString())
//        file.parentFile.mkdirs()
//        file.writeText(allText)
    }

    class PipelineInfo(val pipeline:Document,val noun:String,val msgType:String)

    private fun convertPipeline(name:String,text: String):PipelineInfo {
        val doc = Document.parse("{pipeline:${text}}")
        val pipeline = doc["pipeline"] as List<Document>
        pipeline.forEach {
            val step = it
            if (step["\$project"]!=null) {
                val proj = step["\$project"] as Document
                val newProjCont = proj["m:Payload"]
                val newProj = Document()
                step["\$project"] = newProj
                newProj["noun"] = (proj["m:Header"] as Document)["m:Noun"]
                newProj["verb"] =  (proj["m:Header"] as Document)["m:Verb"]
                newProj["type"] = proj["@type"]
                newProj["model"] = newProjCont

                BsonUtils.getDocumentsRecursiveFromValue(newProjCont).toList().forEach {
                    val obj = it.document

                    var hasType: Boolean = false
                    var hasOther: Boolean = false

                    BsonUtils.getProperties(obj).toList().forEach {
                        if (it.name == "@id") {
                        } else if (it.name == "@type") {
                            hasType = true
                        } else {
                            hasOther = true
                        }
                    }
                    BsonUtils.getProperties(obj).toList().forEach {


                        var value = it.value
                        if (value is String) {
                            value = value.replace("astu:", "")
                        }
                        val propName = it.name.replace("astu:", "")
                        obj.remove(it.name)


                        if (propName == "@id") {
                            if (value is Document) {
                                if (value["\$concat"] != null) {
                                    value = (value["\$concat"] as List<*>).last().toString()
                                }
                            }
                        }

                        obj[propName] = value


                        if (propName == "@type") {
                            if (name.lowercase().contains("удаление")){
                                obj["@action"] = "delete"
                            }
                            else if (hasType) {
                                if (hasOther) {
                                    obj["@action"] = "create"
                                }
                            }
                        }
                    }

                }
            }
        }

        return PipelineInfo(doc,"","")

    }

    val ruleTemplate = """
import { Rule } from "_sys/classes/Rule";
import * as sysCol from '_sys/collections';

export var rule:Rule = {
  "src": __filename,
  input: "in_{inType}",
  output: sysCol.model_Input,
  idSource:"КИСУР",
  pipeline: {pipeline}
}
"""
    private fun writePipeline (name: String,pipelineInfo: PipelineInfo ){
        val outText = StringBuilder()
//        Logger.traceData(name)
        if (name=="УдалениеТрансформатора.js"){
            val cc = ""
        }
        val inType = name.replace(".js","")
        outText.appendLine("[")
        outText.appendLine("{ \$replaceRoot: { newRoot: \"\$payload\" } },")
        var q=""
        (pipelineInfo.pipeline["pipeline"] as List<Document>).forEach {
            outText.append(q)
            outText.appendLine(BsonUtils.toJsonString(it))
            q = ","
        }
        outText.appendLine("]")

        val rule = ruleTemplate
            .replace("{pipeline}",outText.toString())
            .replace("{inType}",inType)
            .replace("astu:","") // не везде срослось при обработке полей, поэтому еще так



        val path  = Paths.get(targetFolder,"$inType.ts")
        val file  = File(path.absolutePathString())
        file.parentFile.mkdirs()

        file.writeText(rule)

    }
}