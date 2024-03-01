package cc.datafabric.adapter.lib.mapping



import java.util.*
import cc.datafabric.adapter.lib.sys.*
//import cc.datafabric.adapter.lib.common.*
import cc.datafabric.adapter.lib.data.DataClass
import cc.datafabric.adapter.lib.common.*

class SkKeyMap (private val direction: KeyStore.Direction) : DataKeyMap(direction) {
    override fun generateNewKey(originalValue: String): String {
         return if (direction == KeyStore.Direction.Outgoing){
            "_$originalValue"
        }else{
            originalValue.drop(1)
        }
    }
}
