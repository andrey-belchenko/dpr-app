package cc.datafabric.exchange.app.con.sk11.rest.incoming


import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.exchange.ExchangeErrorLogger
import cc.datafabric.adapter.lib.exchange.ExchangeStatusStore
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.util.*
import kotlin.system.exitProcess

object Consumer {

    //todo предполагалось что опрос разных API будет единообразным, но это не так
    // переделать, упростить, из общего тут можно оставить только таймер, остальное реализовать отдельно для каждой задачи чтобы не было путаницы

    class  TaskDefinition(val action: ()->String,val tag:String="",val resultCleaner: ((Document)->Document)?=null,val defaultResultProcessing:Boolean =true)

    private val timerInterval: Long = Config.get(ConfigNames.skConsumingIntervalMs).toLong()

    fun consume(tasks:Iterable<TaskDefinition>, processor: (TaskResult) -> Unit) {
        ExchangeStatusStore.setProcessorStatusWaiting()
        startTimer(tasks, processor)
    }

    private val timer = Timer()
    private fun startTimer(tasks:Iterable<TaskDefinition>,processor: (TaskResult) -> Unit) {
        val timerTask = object : TimerTask() {
            override fun run() {
                if (!ExchangeStatusStore.getProcessorDisabled()) {
                    action(tasks, processor)
                }
                startTimer(tasks,processor)
            }
        }
        timer.schedule(timerTask, timerInterval)
    }

    private var resultCache:MutableMap<TaskDefinition,String> = mutableMapOf()
    private var cleanResultCache:MutableMap<TaskDefinition,String> = mutableMapOf()
    private fun action(tasks:Iterable<TaskDefinition>,processor: (TaskResult) -> Unit) {
        tasks.forEach {task->
            action(task, processor)
        }

    }
    private fun action(task:TaskDefinition,processor: (TaskResult) -> Unit) {
        Logger.timerActionStarted()
        Logger.traceFun {

            try {

                if (ExchangeStatusStore.getProcessorStatus()==null){
                    resultCache.clear()
                    cleanResultCache.clear()
                }
                var value = task.action()

                if (task.defaultResultProcessing){
                    var changed = false
                    // проверка на наличие изменений чтобы не отправлять информацию в БД лишний раз
                    if (value!=""){
                        if (resultCache[task] != value) {
                            resultCache[task] = value!!
                            changed = true
                            // в некоторых случаях текст ответа отличается при отсутствии значимых изменений,
                            // может быть предусмотрена очистка, чтобы исключить незначимые изменения
                            if (task.resultCleaner != null) {
                                changed = false
                                val doc = task.resultCleaner!!(Document.parse(value))
                                val cleanValue = doc.toJson()
                                if (cleanResultCache[task] != cleanValue) {
                                    cleanResultCache[task] = cleanValue!!
                                    value = cleanValue
                                    changed = true
                                }
                            }
                        }
                    }
                    if (changed) {
                        Logger.workStarted()
                        ExchangeStatusStore.setProcessorStatusProcessing()
                        val taskResult = TaskResult(task, value)
                        processor(taskResult)
                        ExchangeStatusStore.setProcessorCompletionTime()
                        Logger.workFinished()
                    } else {
                        ExchangeStatusStore.setProcessorStatusWaiting()
                    }
                }else{
                    if (value!=""){
                        ExchangeStatusStore.setProcessorStatusProcessing()
                        ExchangeStatusStore.setProcessorCompletionTime()
                    }else{
                        ExchangeStatusStore.setProcessorStatusWaiting()
                    }
                }

            } catch (ex: Exception) {
                Logger.status("error")
                ExchangeStatusStore.setProcessorStatusError()
                ExchangeStatusStore.setProcessorErrorTime()
                ex.printStackTrace()
//                val errorInfo = ex.stackTraceToString()
//                ExchangeMessageLogger.log(errorInfo,"error")
                ExchangeErrorLogger.log(ex.message, ex.stackTraceToString())
                exitProcess(1)
            }
        }
        Logger.timerActionFinished()
    }

    class TaskResult(
        val taskDefinition:TaskDefinition,
        val value:  String
    )

}