package cc.datafabric.adapter.sandbox.app.sk.ping

import cc.datafabric.adapter.lib.sk.client.SkModelClient
import cc.datafabric.adapter.lib.sys.Logger
import java.util.*



fun main(args: Array<String>) {
    startTimer()
}



private val timerInterval: Long = 1000 * 60
private val timer = Timer()



private fun startTimer() {
        val timerTask = object : TimerTask() {
            override fun run() {
                action()
            }
        }
        timer.schedule(timerTask, timerInterval)
}

private fun action() {
    try {
       Logger.trace("query"){
           SkModelClient.getActualModelVersion()
       }
        Logger.status("ОК")
    } catch (ex: Exception) {
        Logger.status("ERROR")
        Logger.status(ex.message.toString())

    }
    startTimer()
}




