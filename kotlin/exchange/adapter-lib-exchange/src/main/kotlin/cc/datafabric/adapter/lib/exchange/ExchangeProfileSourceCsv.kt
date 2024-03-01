package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Logger
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.nio.file.Paths

class ExchangeProfileSourceCsv : IExchangeProfileSource {

    private fun readFile(name:String): List<Map<String,String>> {
        return Logger.traceFun {
            val path = Paths.get(ExchangeSettingsRepository.getProfileSettingsDir(), "$name.csv").toUri()
            val file = File(path)
            return@traceFun csvReader().open(file) {
                readAllWithHeaderAsSequence().toList()
            }
        }
    }

    override fun getProperties():Map<String, ExchangeProfile.PropertyInfo>{
        val props  = mutableMapOf<String, ExchangeProfile.PropertyInfo>()
        getPropertiesFromCsv("profile",props)
        getPropertiesFromCsv("profileExtension",props)
        return props
    }

    private fun getPropertiesFromCsv(name:String,props:MutableMap<String, ExchangeProfile.PropertyInfo>):Map<String, ExchangeProfile.PropertyInfo> {
        readFile(name).forEach {
            var isMultiple = listOf("0..n", "1..n").contains(it["multiplicity"]!!)

            //todo есть такие свойства, одновременно литерал и ссылка
            // при этом multiplicity "0..n"
            // обратная связь не находится
            // ставлю им isMultiple = false
            // сейчас предполагается что не должно быть: связей 0..n без обратной связи, литерал массивов, связей многие ко многим
            // если такие случаи бывают требуется анализ и пересмотр решения

//                {
//                    "class" : "PowerTransformerEnd",
//                    "prop" : "PowerTransformerEnd.ratedU",
//                    "propType" : "Voltage",
//                    "propTypeNameSpace" : "http://ontology.adms.ru/UIP/md/2021-1#",
//                    "multiplicity" : "0..n",
//                    "isLiteral" : true,
//                    "_id" : ObjectId("63c758b3f172bd34f670e2da")
//                }
            val isLiteral = it["isLiteral"]=="true"
            if (isLiteral){
                isMultiple = false
            }
            val key = "${it["className"]}.${it["predicate"]}"
            val propInfo = ExchangeProfile.PropertyInfo(
                className = it["className"]!!,
                name = it["predicate"]!!,
                id = it["predicate"]!!.replace(".", "_"),
                isMultiple = isMultiple,
                typeName = it["range"]!!,
                isLiteral = isLiteral
            )
            props[key]=propInfo
        }
        return  props
    }


    override fun getInverseMap():Map<String,String>{
        val inversionMap = mutableMapOf<String,String>()
        inversionMap.clear()
        readFile("inversionExtension").forEach {
            inversionMap[it["predicate"]!!.replace(".", "_")] = it["inverseOf"]!!.replace(".", "_")
        }
        return inversionMap

    }

    override fun getClassNamespaces(): Map<String, String> {
        val map = mutableMapOf<String,String>()
        map.clear()
        readFile("classNamespace").forEach {
            map[it["className"].toString()] = it["namespacePrefix"]!!
        }
        return map
    }

    override fun getPropertyNamespaces(): Map<String, String> {
        val map = mutableMapOf<String,String>()
        map.clear()
        readFile("predicateNamespace").forEach {
            map[it["predicateName"]!!.replace(".", "_")] = it["namespacePrefix"]!!
        }
        return map
    }


}