package cc.datafabric.extensions

import cc.datafabric.adapter.lib.exchange.ExchangeProfile
import cc.datafabric.adapter.lib.exchange.IExchangeProfileSource
import cc.datafabric.core.Profile


class ExchangeProfileSourcePlatform (private val profile: Profile): IExchangeProfileSource {

    private val props  = mutableMapOf<String, ExchangeProfile.PropertyInfo>()
    init {
        profile.classes.forEach { cls ->

            cls.value.links.forEach { link ->

                val propInfo = ExchangeProfile.PropertyInfo(
                    className = cls.key.split("#").last(),
                    name =  link.key.split("#").last(),
                    id = link.key.split("#").last().replace(".", "_"),
                    isMultiple = link.value.counts.max>1,
                    typeName = null,
                    //  link.value.ranges содержи все возможные значения, а нужно одно определенное в профиле
                    //  в расширении это свойство используется для вычисления обратных связей
                    //  в текущем решении передаем обратные связи из profile поэтому пока можно оставить null
                    inverseOf = link.value.inverse?.split("#")?.last()?.replace(".", "_"),
                    isLiteral = false
                )
                val key = "${propInfo.className}.${propInfo.name}"
                props[key]=propInfo
            }

            cls.value.attributes.forEach {attr->
                val propInfo = ExchangeProfile.PropertyInfo(
                    className = cls.key.split("#").last(),
                    name =  attr.key.split("#").last(),
                    id = attr.key.split("#").last().replace(".", "_"),
                    isMultiple = false,
                    typeName = attr.value.datatype.split("#").last(),
                    inverseOf =null,
                    isLiteral = true
                )
                val key = "${propInfo.className}.${propInfo.name}"
                props[key]=propInfo
            }
        }
    }
    override fun getProperties():Map<String, ExchangeProfile.PropertyInfo>{
        return props
    }


    override fun getInverseMap():Map<String,String>{
        return mutableMapOf<String,String>()
    }

    override fun getClassNamespaces(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun getPropertyNamespaces(): Map<String, String> {
        TODO("Not yet implemented")
    }
}