package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.exchange.data.ConstNamespaces
import cc.datafabric.adapter.lib.exchange.data.Namespace
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.nio.file.Paths
import cc.datafabric.adapter.lib.sys.ConfigNames
object ExchangeProfile {

    class ClassInfo (val name:String){
        val properties:MutableMap<String,PropertyInfo> = mutableMapOf()
    }

    class PropertyInfo (
        val className: String,
        val name:String,
        val id:String,
        val typeName:String?,
        val isMultiple:Boolean,
        val isLiteral:Boolean,
        var inverseOf:String? =  null
    )

    var profileSource:IExchangeProfileSource =  ExchangeProfileSourceCsv()

    private val profileMap = mutableMapOf<String,ClassInfo>()
    private var inversionMap = mapOf<String,String>()
    private var props = mapOf<String,PropertyInfo>()
    private var propNs = mapOf<String,String>()
    private var classNs = mapOf<String,String>()
//    className,predicate,range,multiplicity,isLiteral,inverseOf

    fun initialize() {
        props = profileSource.getProperties()
        propNs = profileSource.getPropertyNamespaces()
        classNs = profileSource.getClassNamespaces()
        profileMap.clear()
        props.values.groupBy { it.className }.forEach{
            val classInfo = ClassInfo(it.key)
            profileMap[it.key]= classInfo
            it.value.forEach {pi->
                classInfo.properties[pi.id]= pi
            }
        }
        initInverse()
    }


    fun getPropertyNamespace(propertyName: String): Namespace {
        if (!propNs.containsKey(propertyName)){
            return  ConstNamespaces.cim
        }
        return  ConstNamespaces.getByPrefix(propNs[propertyName]!!)!!
    }

    fun getClassNamespace(className: String): Namespace {
        if (!classNs.containsKey(className)){
            return  ConstNamespaces.cim
        }
        return  ConstNamespaces.getByPrefix(classNs[className]!!)!!
    }

    private fun initInverse(){
        inversionMap = profileSource.getInverseMap()
        props.values.forEach { prp->
            if (inversionMap.containsKey(prp.id)){
                prp.inverseOf = inversionMap[prp.id]
            }
            else{
                if (prp.inverseOf==null && prp.typeName!=null){
                    // todo можно ускорить, индексировать
                    var inverse = props.values.filter {
                        it.className==prp.typeName && it.typeName==prp.className
                    }

                    if (inverse.count()>1){
                        inverse = inverse.filter { it.id.split('_').last() == prp.id.split('_').last() }
                    }

                    if (inverse.count()==1){
                        prp.inverseOf=inverse.first().id
                    }
                }

            }

        }

    }


    fun ensureExists (className: String,propertyName: String){
        if (profileMap[className]==null){
            throw Exception("Class $className does not exist")
        }
        if (profileMap[className]!!.properties[propertyName]==null){
            throw Exception("Property $className.$propertyName does not exist")
        }
    }

    fun ensureExists (className: String) {
        if (profileMap[className] == null) {
            throw Exception("Class $className does not exist")
        }
    }

    fun getInverseName(className: String,propertyName: String):String?{
        ensureExists(className,propertyName)
        return profileMap[className]!!.properties[propertyName]!!.inverseOf
    }
    fun isMultiple(className: String,propertyName: String):Boolean{
        ensureExists(className,propertyName)
        return profileMap[className]!!.properties[propertyName]!!.isMultiple
    }

    fun isLiteral(className: String,propertyName: String):Boolean {
        ensureExists(className, propertyName)
        return profileMap[className]!!.properties[propertyName]!!.isLiteral
    }


    fun getClass(className: String):ClassInfo {
        return  profileMap[className]!!
    }
    fun hasClass(className: String):Boolean{
        return profileMap.containsKey(className)
    }
    fun hasProperty(className: String,propertyName: String):Boolean{
        if (profileMap[className]==null){
            val xx =""
        }
        return  profileMap[className]!!.properties.containsKey(propertyName)
    }

    fun getPropertyType(className: String,propertyName: String):String{
        return profileMap[className]!!.properties[propertyName]!!.typeName!!
    }
}