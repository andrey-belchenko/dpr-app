package cc.datafabric.adapter.lib.mapping

import cc.datafabric.adapter.lib.data.ConstNamespaces
import cc.datafabric.adapter.lib.data.Namespace
import cc.datafabric.adapter.lib.data.DataClass
import cc.datafabric.adapter.lib.data.DataClassProperty
import cc.datafabric.adapter.lib.data.DataProperty
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.nio.file.Paths

//todo мутновато, продумать
object DataPropertyMapConfig {



    private val externalNamespaces by lazy {
        readFile("external-namespaces").map {
            it["prefix"].toString() to  Namespace(it["prefix"].toString(),it["uri"].toString())
        }.toMap()
    }


    private const val defaultNamespacePrefix = "cim" //Config.get("adpDefaultNamespace")
    private  val defaultNamespace = ConstNamespaces.getByPrefix(defaultNamespacePrefix)!!

    private  val propMapping by lazy {
        val list = mutableListOf<Pair<DataClassProperty, DataClassProperty>>()
        readFile("properties-mapping").forEach {

            val internal = DataClassProperty(
                DataClass(
                    defaultNamespace,
                    it["internalClass"].toString()
                ),
                DataProperty(
                    defaultNamespace,
                    it["internalProperty"].toString()
                )
            )

            val externalClassParts =it["externalClass"].toString().split(':')
            val externalPropParts =it["externalProperty"].toString().split(':')
            val external = DataClassProperty(
                DataClass(
                    externalNamespaces[externalClassParts[0]]!!,
                    externalClassParts[1]
                ),
                DataProperty(
                    externalNamespaces[externalPropParts[0]]!!,
                    externalPropParts[1]
                )
            )
            list.add(Pair(internal,external))
        }
        list
    }

     val outgoingPropertyMap by lazy {
         val map = DataPropertyMap()
         propMapping.forEach {
             map.add(it.first, it.second)
         }
         map
     }

    val incomingPropertyMap by lazy {
        val map = DataPropertyMap()
        propMapping.forEach {
            map.add(it.second, it.first)
        }
        map
    }


    private fun readFile(name:String): List<Map<String,String>>  {
        return Logger.traceFun {
            val dir = Config.get("adpMappingSettingsDir")
            val path = Paths.get(dir,"$name.csv").toUri()
            val file =  File(path)
            return@traceFun csvReader().open(file){
                readAllWithHeaderAsSequence().toList()
            }
        }
    }


}