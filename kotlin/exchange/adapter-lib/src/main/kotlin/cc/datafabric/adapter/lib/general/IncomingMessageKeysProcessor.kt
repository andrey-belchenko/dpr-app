package cc.datafabric.adapter.lib.general


import cc.datafabric.adapter.lib.common.*
import java.util.*
import cc.datafabric.adapter.lib.sys.*
import cc.datafabric.adapter.lib.common.tools.LoggerExtension.traceObject
object IncomingMessageKeysProcessor {
     fun process(message: IncomingOutMessage) {
         Logger.traceFunBeg()
         var isChangeOnly = true
         var isDelete = message.getVerb() == "delete"
         message.getEntities().toList().forEach {
             if (processEntity(it,isDelete) == KeyStatus.Generated) {
                 isChangeOnly = false
             }
         }
         if (isChangeOnly && message.getVerb() == "create") {
             message.setVerb("change")
         }
         Logger.traceFunEnd()
         Logger.traceObject(message)
     }



    private enum class KeyStatus {
        Found, Generated
    }

    // todo УБРАТЬ!!!!
    val notFoundKeys = mutableListOf<String>()

    private fun processEntity(entity: IncomingOutMessageEntity, isDelete:Boolean): KeyStatus {
        val platformKeyInfo = queryKey(entity)
        var platformKey = platformKeyInfo?.key
        Logger.traceData("found key: $platformKey")
        var isNew = false
        if (platformKey== null) {
            notFoundKeys.add(entity.getTypeName() +"\t"+ entity.getSourceKey()!!)
            platformKey = /*entity.getTypeName()+"_"+*/ UUID.randomUUID().toString()
            Logger.traceData("generated key: $platformKey")
            isNew = true
        }
        entity.setPlatformKey(platformKey)
        var result = KeyStatus.Found
        if (isNew) {
            saveKeyMapping(entity)
            result = KeyStatus.Generated
        }
        if (isDelete){
            deleteKeyMapping(entity)
        }
        return result
    }

    private fun queryKey(entity: IncomingOutMessageEntity): KeyInfo? {
        return KeyStore.queryPlatformKey(
            entity.getSourceSystem(),
            entity.getTypeIri(),
            entity.getSourceKey()!!
        )
    }

    private fun saveKeyMapping(entity: IncomingOutMessageEntity
    ) {
        Logger.traceFun {
            KeyStore.saveKey(
                sourceSystem = entity.getSourceSystem(),
                sourceKeyInfo = KeyInfo( entity.getTypeIri(), entity.getSourceKey()!!,null),
                platformKeyInfo = KeyInfo( entity.getTypeIri(),entity.getPlatformKey()!!, null)
            )
        }
    }

    private fun deleteKeyMapping(entity: IncomingOutMessageEntity
    ) {
        Logger.traceFun {
            KeyStore.deleteKey(
                entity.getSourceSystem(),
                entity.getSourceKey()!!,
                KeyStore.Direction.Incoming
            )
        }
    }


}