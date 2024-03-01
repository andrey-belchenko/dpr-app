package cc.datafabric.exchange.app.processor.api

import cc.datafabric.adapter.lib.exchange.ScriptsRepository
import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object PythonScriptsUtils {

    private fun isWindows():Boolean {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        return os.contains("win")
    }

    private fun getPythonName():String {
        return if (isWindows()) {
            "python"
        } else {
            "python3"
        }
    }



//    fun getScriptParamsJson (projectName:String, scriptName:String):String{
//        val scriptText =  getScriptText(projectName,scriptName)
//        val matchResult = scriptParamsRegex.find(scriptText)
//        val variableDefinition = matchResult?.groups?.get(0)?.value
//        val scriptForEval = "$variableDefinition\nprint(scriptParams)"
//        val value =  executeScript(scriptForEval)
//        val params = Document.parse(value)
//        params.remove("mongoUri")
//        return BsonUtils.toJsonString(params)
//    }'

    private fun getVariableDefinitionPosition(scriptText: String, variableName: String): Pair<Int,Int> {
        val lines = scriptText.split("\n")

        var lineNumber = 0
        var assignmentFound = false
        var assignmentIndex = 0

        while (!assignmentFound) {
            if (lines.count() == lineNumber) {
                throw IllegalArgumentException("Variable $variableName not found")
            }
            val line = lines[lineNumber]
            if (line.startsWith("$variableName ", false) || line.startsWith("$variableName=", false)) {
                assignmentFound = true
            } else {
                assignmentIndex += line.length + 1

            }
            lineNumber++
        }
        var braceCount = 0
        var endIndex = assignmentIndex
        while (endIndex < scriptText.length) {
            when (scriptText[endIndex]) {
                '{' -> {
                    braceCount++
                }
                '}' -> braceCount--
            }
            if (braceCount == 0 && scriptText[endIndex] == '}') {
                break
            }
            endIndex++
        }
        if (braceCount == 0) {
            return Pair(assignmentIndex, endIndex + 1)
        } else {
            throw IllegalArgumentException("No matching closing brace found for variable $variableName")
        }
    }

    private fun extractVariableDefinition(scriptText: String, variableName: String): String {
        val position =  getVariableDefinitionPosition(scriptText,variableName)
        return scriptText.substring(position.first, position.second)
    }
    fun getScriptVarJson (projectName:String, scriptName:String, varName:String):String {
        val scriptText = getScriptText(projectName, scriptName)
        val variableDefinition = extractVariableDefinition(scriptText,varName)
        val scriptForEval = "$variableDefinition\nprint($varName)"
        val value = executeScript(scriptForEval)
        val params = Document.parse(value)
        params.remove("mongoUri")
        return BsonUtils.toJsonString(params)
    }
    private fun getScriptText(projectName:String, scriptName:String):String {
        val projectPath = ScriptsRepository.getProjectPath(projectName)
        val scriptPath = buildFullPath(projectPath, scriptName)
        val scriptFile = File(scriptPath)
        return scriptFile.readText()
    }

    fun runScript(projectName:String, scriptName:String,params:Document?) {
        val projectPath = ScriptsRepository.getProjectPath(projectName)
        val scriptPath = buildFullPath(projectPath, scriptName)
        val scriptFile = File(scriptPath)
        val originalScriptText = scriptFile.readText()
        var scriptText = originalScriptText
        if (params != null) {
            params["mongoUri"] =  Config.get(ConfigNames.mongoUri)
            val paramsCode = BsonUtils.toJsonString(params)
            val setParamsCmd = "scriptParams=$paramsCode"
            val varName = "scriptParams"
            val variableDefinition = extractVariableDefinition(scriptText,varName)
            scriptText =   scriptText.replace(variableDefinition,setParamsCmd)
        }
        val tempScriptFileName = UUID.randomUUID().toString() + ".py"
        val tempScriptFilePath = Path(scriptFile.parentFile.absolutePath,tempScriptFileName).absolutePathString()
        executeScript(scriptText,projectPath,tempScriptFilePath)
    }

    private fun buildFullPath(basePath: String, relPath: String): String {
        val file = File(basePath)
        val parts = relPath.split("/")
        var result = file
        for (part in parts) {
            result = File(result, part)
        }
        return result.path
    }


    private fun executeScript(scriptText:String, pythonPath:String?=null, tempScriptFilePath:String?=null):String {
        var cmd = "-"
        if (tempScriptFilePath!=null){
            val tempScriptFile = File(tempScriptFilePath)
            tempScriptFile.writeText(scriptText)
            cmd = tempScriptFilePath
        }

        val processBuilder = ProcessBuilder(getPythonName(), cmd)
        processBuilder.apply {
            if (pythonPath != null) {
                environment()["PYTHONPATH"] = pythonPath
            }
            environment()["PYTHONIOENCODING"] = "UTF-8"
        }
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        if (tempScriptFilePath==null){
            process.outputStream.writer(Charset.forName("UTF-8")).use { it.write(scriptText) }
        }
        val reader = BufferedReader(InputStreamReader(process.inputStream, "UTF-8"))
        val output = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            output.append(line).append('\n')
            Logger.trace(line ?: "")
        }
        val exitValue = process.waitFor()
        if (tempScriptFilePath!=null){
            val tempScriptFile = File(tempScriptFilePath)
            tempScriptFile.delete()
        }
        if (exitValue != 0) {
            throw RuntimeException("Python script execution error: $output")
        }
        return output.toString()
    }


}