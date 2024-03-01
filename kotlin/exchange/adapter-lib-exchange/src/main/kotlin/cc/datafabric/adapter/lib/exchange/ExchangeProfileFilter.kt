package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.nio.file.Paths
import cc.datafabric.adapter.lib.sys.ConfigNames

class ExchangeProfileFilter(private val folder: String) {

    private  val classes = mutableSetOf<String>()
    private  val excludeClasses = mutableSetOf<String>()
    private  val properties = mutableMapOf<String,MutableSet<String>>()

    private val isAny = folder == ""
    private var isAnyProperty:Boolean = false
    private var hasExcluded:Boolean = false
    init {
        classes.clear()
        properties.clear()
        if (!isAny){
            getPropertiesFromCsv()
            getClassesFromCsv()
            getExcludedClassesFromCsv()
        }
    }

    fun hasUpdateClass(className: String):Boolean {
        if (isAny) {
            return ExchangeProfile.hasClass(className)
        }
        return  hasCreateClass(className) || properties.containsKey(className)
    }

    fun hasCreateClass(className: String):Boolean{
        if (isAny){
            return  ExchangeProfile.hasClass(className)
        }
        return if (hasExcluded) {
            !excludeClasses.contains(className) && ExchangeProfile.hasClass(className)
        } else {
            return classes.contains(className)
        }
    }
    fun hasProperty(className: String, propertyName:String):Boolean {
        if (
            isAny
            || isAnyProperty
            || (hasExcluded && !excludeClasses.contains(className))
            || (properties[className]!=null && properties[className]!!.contains("*"))
        ) {
            if (!ExchangeProfile.hasProperty(className, propertyName)) {
                return false
            }
            if (!ExchangeProfile.isMultiple(className, propertyName)) {
                return true
            }
            return ExchangeProfile.getInverseName(className, propertyName) != null
        }
        if (!properties.containsKey(className)) {
            return false
        }
        return properties[className]!!.contains(propertyName)
    }

    private fun getFile(name:String): File {
        val settingsDir = ExchangeSettingsRepository.getSettingsDir()
        return  File( Paths.get(settingsDir, "profile-filter",folder, "$name.csv").toUri())
    }
    private fun checkFileExists(name:String): Boolean {
        return Logger.traceFun {
            return@traceFun getFile(name).exists()
        }
    }
    private fun readFile(name:String): List<Map<String,String>> {
        return Logger.traceFun {
            val file = getFile(name)
            return@traceFun csvReader().open(file) {
                readAllWithHeaderAsSequence().toList()
            }
        }
    }


    private fun getClassesFromCsv() {
        val name = "classes"
        if (checkFileExists(name)) {
            readFile(name).forEach {
                classes.add(it["class"]!!)
            }
        }
    }



    private fun getExcludedClassesFromCsv() {
        val name = "exclude-classes"
        if (checkFileExists(name)) {
            readFile(name).forEach {
                excludeClasses.add(it["class"]!!)
            }
            hasExcluded = true
        }
    }



    private fun getPropertiesFromCsv() {
        val name = "properties"
        if (checkFileExists(name)) {
            isAnyProperty = false
            readFile(name).forEach {
                val className = it["class"]!!
                if (!properties.containsKey(className)) {
                    properties[className] = mutableSetOf()
                }
                properties[className]!!.add(it["predicate"]!!)
            }
        } else {
            isAnyProperty = true
        }
    }

}