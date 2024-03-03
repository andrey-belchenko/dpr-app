package cc.datafabric.exchange.cim.model

import kotlin.reflect.KProperty

//class LinksDelegate {
//}

class LinksDelegate <T: ModelObject>(private val inverseProperty: KProperty<*>?=null) {
    private  var value: Links<T>?=null
    operator fun getValue(thisRef: ModelObject?, property: KProperty<*>): Links<T> {
        if (value==null){
            value = Links(thisRef!!,inverseProperty)
        }
        return value as Links<T>
    }
//    operator fun setValue(thisRef: ModelObject?, property: KProperty<*>, value: Links<T>) {
//        this.value = value
//    }
}
