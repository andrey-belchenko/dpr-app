package cc.datafabric.adapter.lib.sk.client

import cc.datafabric.adapter.lib.common.StatusStore
import cc.datafabric.adapter.lib.common.tools.MessageLogger
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import java.util.*
import kotlin.system.exitProcess

object SkDiffConsumer {


    private const val lastVersionVar = "lastVersion"
    private const val targetVersionVar = "targetVersion"
    private val timerInterval: Long = Config.get("adpConsumingIntervalMs").toLong()
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

                val lastVersion = StatusStore.getInt(lastVersionVar)
                if (lastVersion!=null){
                    var targetVersion = StatusStore.getInt(targetVersionVar)
                    if (targetVersion==null){
                        targetVersion = SkModelClient.getActualModelVersion()
                        Logger.status("found actualVersion:$targetVersion")
                    }else{
                        Logger.status("found targetVersion preset:$targetVersion")
                    }
                    if (targetVersion!=lastVersion) {
                        Logger.status("lastVersion:$lastVersion -> targetVersion:$targetVersion")
                        val diff = SkModelClient.getModelVersionsDifference(lastVersion, targetVersion)
                        diffProcessor(DiffInfo(diff,lastVersion,targetVersion))
                        StatusStore.set(lastVersionVar, targetVersion)
                    }else{
                        Logger.status("lastVersion and targetVersion is $lastVersion")
                    }
                }else{
                    Logger.status("lastVersion not found")
                }
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

    class DiffInfo (val diffString:String,val lastVersion:Int, val targetVersion:Int )

}