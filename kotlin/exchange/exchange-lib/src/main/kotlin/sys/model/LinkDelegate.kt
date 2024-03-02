package cc.datafabric.linesapp.sys.model
import kotlin.reflect.KProperty

class LinkDelegate<T : ModelObject?>(private val inverseProperty: KProperty<*>? = null) {
    private var value: ModelObject? = null
    operator fun getValue(thisRef: ModelObject?, property: KProperty<*>): T {
        return value as T
    }


    operator fun setValue(thisRef: ModelObject?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        this.value = value
        if (inverseProperty != null) {
            if (oldValue != value) {
                if (oldValue!=null){
                    ReflectionUtils.removeLink(oldValue, inverseProperty, thisRef!!)
                }
                if (value!=null){
                    ReflectionUtils.addLink(value, inverseProperty, thisRef!!)
                }
                thisRef?.changed()
            }
        }

    }
}