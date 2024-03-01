package cc.datafabric.adapter.lib.mapping



import java.util.*
import cc.datafabric.adapter.lib.sys.*
//import cc.datafabric.adapter.lib.common.*
import cc.datafabric.adapter.lib.data.DataClass
import cc.datafabric.adapter.lib.common.*

abstract class DataKeyMap (private val direction: KeyStore.Direction)  {


    companion object{
        private val sourceSystem = Config.get("adpSourceSystem")
    }

    var saveToStorage = true
    private val cache = mutableMapOf<String,KeyInfo>()
    private val keysToDelete = mutableListOf<String>()
    fun get(originalValue:String, isDelete:Boolean):KeyInfo? {
        var newKeyInfo = cache[originalValue]
        if (newKeyInfo!=null){
            return  newKeyInfo
        }
        newKeyInfo = KeyStore.queryKey(sourceSystem, originalValue, direction)
        if (newKeyInfo!=null){
            cache[originalValue] = newKeyInfo
        }
        //todo еще подумать над случаем с удалением
        if (isDelete){
            if (!keysToDelete.contains(originalValue)){
                keysToDelete.add(originalValue)
            }
        }
        return newKeyInfo
    }

    fun commitDeleted(){
        if (!saveToStorage) return
        keysToDelete.forEach {
            KeyStore.deleteKey(sourceSystem,it,direction)
        }
    }

    abstract fun generateNewKey(originalValue:String):String

    fun get(originalDataClass: DataClass, newDataClass: DataClass, originalValue:String):KeyInfo?{

        var newKeyInfo =get(originalValue,false)
        if (newKeyInfo != null) {
            return newKeyInfo
        }
//        val newValue = when (direction) {
//            //todo применимо только для СК-11, в случае взаимодействия другими системами - доработать
//            KeyStore.Direction.Outgoing -> "_${UUID.randomUUID()}"
//            KeyStore.Direction.Incoming -> "${newDataClass.name}_${UUID.randomUUID()}"
//        }
        val newValue = generateNewKey(originalValue)
        newKeyInfo = KeyInfo(
            newDataClass.getUri(),
            newValue,
            isGenerated = true
        )
        cache[originalValue] = newKeyInfo

        if (saveToStorage){
            KeyStore.saveKey(
                sourceSystem,
                origKeyInfo = KeyInfo(
                    originalDataClass.getUri(),
                    originalValue,
                    isGenerated = null
                ),
                newKeyInfo,
                direction
            )
        }



        return newKeyInfo
    }
}
