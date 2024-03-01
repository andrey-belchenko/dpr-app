package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Logger
import java.util.*
import kotlin.system.exitProcess

// todo переделать все места где используется таймер на использование этого класса при необходимости доработать этот класс
class ExchangeTimer(private val interval: Long) {

    private val timer = Timer()

    fun run(handler: () -> Unit) {
        try {
            ExchangeStatusStore.setProcessorStatusWaiting()
        } catch (ex: Exception) {
            handleException(ex)
        }
        Logger.traceFun {
            startTimer(handler)
        }
    }

    private fun startTimer(handler: () -> Unit) {
        Logger.traceFun {
            val timerTask = object : TimerTask() {
                override fun run() {
                    try {
                        Logger.timerActionStarted()
                        action(handler)
                        startTimer(handler)
                        Logger.timerActionFinished()
                    } catch (ex: Exception) {
                        handleException(ex)
                    }
                }
            }
            timer.schedule(timerTask, interval)
        }
    }


    private fun action(handler: () -> Unit) {

        handler()
    }

    private fun exit(exception: Exception) {
        Logger.status("error")
        exception.printStackTrace()
        Logger.traceBeg("exit")
        exitProcess(1)
    }

    private fun handleException(exception: Exception) {
        try {
            ExchangeErrorLogger.log(exception.message, exception.stackTraceToString())
            ExchangeStatusStore.setProcessorStatusError()
            ExchangeStatusStore.setProcessorErrorTime()
        } catch (_: Exception) {
            exit(exception)
        }
        exit(exception)

    }


}