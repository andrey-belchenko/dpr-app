package cc.datafabric.exchange.app.con.sk11.outgoing

import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.sk.client.SkModelClient
import cc.datafabric.adapter.lib.sk.client.SkClientExtension.getTargetVersionId
import cc.datafabric.adapter.lib.sys.Logger

object App {

    private val timer = ExchangeTimer(5000)

    private fun ping() {
        Logger.traceFun {

            SkModelClient.getActualModelVersion()

            Logger.status("ping: ОК")
        }
    }

    fun main() {

        timer.run {
            // Помогло с MC-202
            ping()
        }

        ExchangeListener.listenCollections(listOf("out_Sk11")) { res ->
            res.changedCollections.forEach { col ->
                if (ExchangeSettingsRepository.loadIfNeed()) {
                    ExchangeProfile.initialize()
                }
                val extraInfo = mutableMapOf<String, Any>(
                    "startTimestamp" to res.lastTimestamp,
                    "endTimestamp" to res.newTimestamp,
                )
                val docs = col.getDocuments().toList()
                ExchangeMessageLogger.log(docs, "in", extraInfo)
                val diff = ExchangeDiffSerializer.getDiffXml(docs, true)
                val version = SkModelClient.getTargetVersionId()
                extraInfo["modelVersion"] = version
                var status = "ОК"
                ExchangeMessageLogger.log(diff, "out-sending", extraInfo)

                try {
                    SkModelClient.changeObjects(version, diff)
                } catch (ex: Exception) {
                    status = "ERROR"
                    throw ex
                }

                extraInfo["modelVersion"] = version
                extraInfo["status"] = status
                ExchangeMessageLogger.log(diff, "out-sent", extraInfo)
            }
        }
    }
}
