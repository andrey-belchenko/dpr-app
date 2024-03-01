package cc.datafabric.adapter.lib.exchange.data

import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames


object ConstNamespaces {
    val rdf = Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", true)
    val dm = Namespace("dm", "http://iec.ch/2002/schema/CIM_difference_model#", true)
    val dmSk = Namespace("dmSk",Config.get(ConfigNames.skDmNamespace), true)
    val md = Namespace("md", "http://iec.ch/TC57/61970-552/ModelDescription/1#", true)
    // todo хардкод?
    val cim = Namespace("cim", "http://iec.ch/TC57/2014/CIM-schema-cim16#", true)
    val astu = Namespace("astu", "http://ontology.adms.ru/UIP/md/2021-1#", true)
    // todo хардкод, нэймспейсы для полей настраиваются, либо вынести это в настройку, либо убрать из настройки неймспейсы полей
    val me = Namespace("me", "http://monitel.com/2014/schema-cim16#", true)
    val rf=  Namespace("rf", "http://gost.ru/2019/schema-cim01#", true)


    private val mapByUri = mapOf(
        rdf.uri to rdf,
        dm.uri to dm,
        md.uri to md,
        cim.uri to cim,
        astu.uri to astu,
        me.uri to me,
        rf.uri to rf,
        dmSk.uri to dmSk
    )

    private val mapByPrefix = mapOf(
        rdf.prefix to rdf,
        dm.prefix to dm,
        md.prefix to md,
        cim.prefix to cim,
        astu.prefix to astu,
        me.prefix to me,
        rf.prefix to rf,
        dmSk.prefix to dmSk
    )

    fun getByUri(uri: String): Namespace? {
        return mapByUri[uri]
    }

    fun getByPrefix(prefix: String): Namespace? {
        return mapByPrefix[prefix]
    }
}