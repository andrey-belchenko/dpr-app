package cc.datafabric.exchange.app.con.platform.outgoing

import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.platform.client.PlatformClientTasks

object App {
    fun main() {
//        val diffBytes = File("C:\\Repos\\datafabric\\mrsk-configuration\\exchange-configuration\\tools\\test\\belchenko\\test-tasks\\MC-273\\diff\\1.xml") .readBytes()
//        PlatformClientTasks.sendDiff(diffBytes)
        ExchangeListener.listenCollections(listOf("out_Platform")) { res ->
            res.changedCollections.forEach { col ->
                val docs = col.getDocuments().toList()
                val diff = ExchangeDiffSerializer.getDiffXml(docs,false)

                val diffBytes = diff.toByteArray()
                val fileId = ExchangeDatabase.uploadFile(diffBytes.inputStream())
                ExchangeMessageLogger.log(diff,"out", mapOf(Names.Fields.fileId to fileId))
                if (ExchangeStatusStore.get(Names.Fields.onlyLogging)==null){
                    PlatformClientTasks.sendDiff(diffBytes)
                }
            }
        }
    }
}
