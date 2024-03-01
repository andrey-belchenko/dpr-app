package cc.datafabric.exchange.app.con.sk11.incoming

import Loader
import cc.datafabric.adapter.lib.exchange.ExchangeMessageLogger

object App {
    fun main() {

        ExchangeSkDiffConsumer.consume { diffInfo ->

            val extraInfo=mapOf (
                "lastVersion"  to  diffInfo.lastVersion,
                "targetVersion" to diffInfo.targetVersion
            )
            val diffForLog = diffInfo.diffString ?: ""
            ExchangeMessageLogger.log(diffForLog, "in", extraInfo)
            Loader.load(diffInfo.diff,diffInfo.filterName,diffInfo.isImport)
        }
    }
}
