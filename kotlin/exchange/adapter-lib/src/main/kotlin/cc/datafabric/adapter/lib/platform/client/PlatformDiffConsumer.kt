package cc.datafabric.adapter.lib.platform.client

import cc.datafabric.adapter.lib.common.StatusStore
import cc.datafabric.adapter.lib.common.TimeUtils.toFormattedString
import cc.datafabric.adapter.lib.common.tools.MessageLogger
import cc.datafabric.adapter.lib.general.MessageAccumulator
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import java.time.ZonedDateTime
import java.util.*
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

object PlatformDiffConsumer {

    private const val lastLoadTimeVar = "lastLoadTime"
    private val timerInterval: Long = Config.get("adpConsumingIntervalMs").toLong()
    private val timestampDelay: Long = Config.get("adpConsumingDelaySec").toLong()
    fun consume(diffProcessor: (DiffInfo) -> Unit) {
        startTimer(diffProcessor)
    }
    private val timer = Timer()
    private fun startTimer(diffProcessor: (DiffInfo) -> Unit) {
        Logger.traceFun {
            val timerTask = object : TimerTask() {
                override fun run() {
                    action(diffProcessor)
                }
            }
            timer.schedule(timerTask, timerInterval)
        }
    }

    private fun action(diffProcessor: (DiffInfo) -> Unit) {
        Logger.traceFun {

            try{

                val startTimestamp = getLastLoadTime()
                val endTimestamp = ZonedDateTime.now().minusSeconds(timestampDelay) // todo костыль чтобы снизить вероятность "коллизий"
                var diff = ""
                val elapsed = measureTimeMillis {
                    diff = PlatformClientTasks.getDiff(startTimestamp, endTimestamp)
                }

//            val diff = "diff on " + endTimestamp.toOffsetDateTime()
                diffProcessor(DiffInfo(diff,startTimestamp.toFormattedString(),endTimestamp.toFormattedString(),elapsed))
                StatusStore.set(lastLoadTimeVar, endTimestamp)
                startTimer(diffProcessor)
            } catch (ex: Exception) {
                Logger.status("error")
                ex.printStackTrace()
                val errorInfo = ex.stackTraceToString()
                MessageLogger.log(errorInfo,"error")
                exitProcess(1)
            }


        }

    }

    private fun getLastLoadTime(): ZonedDateTime {
        return Logger.traceFun {
            return@traceFun StatusStore.getZonedDateTime(lastLoadTimeVar)
                ?: ZonedDateTime.parse("1900-01-01T00:00Z")
        }
    }

    class DiffInfo (val diffString:String,val startTimestamp:String,val endTimestamp:String, val execTimeMs:Long)

}