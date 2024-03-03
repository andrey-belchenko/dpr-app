package cc.datafabric.exchange.cim.model
import cc.datafabric.linesapp.sys.model.ModelObject
import kotlin.reflect.KProperty

class ValueDelegate<T>() {
    private var value: T? = null
    operator fun getValue(thisRef: ModelObject?, property: KProperty<*>): T? {
        return value

    }

    operator fun setValue(thisRef: ModelObject?, property: KProperty<*>, value: T) {
        this.value = value
    }
}