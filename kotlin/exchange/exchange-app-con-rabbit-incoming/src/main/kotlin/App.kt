package cc.datafabric.exchange.app.con.rabbit.incoming

import cc.datafabric.adapter.lib.exchange.ExchangeStatusStore
import cc.datafabric.adapter.lib.exchange.ExchangeStore
import cc.datafabric.adapter.lib.exchange.Names
import cc.datafabric.adapter.lib.rabbit.RabbitConsumer
import cc.datafabric.adapter.lib.sys.Logger
import kotlin.system.exitProcess

object App {
    fun main() {
        try {
            ExchangeStore.useTimer =  true
            ExchangeStore.doMessageLogging = true
            ExchangeStatusStore.setProcessorStatusWaiting()
            RabbitConsumer.consume{
                while (ExchangeStatusStore.getProcessorDisabled()){
                    Thread.sleep(2_000)
                }
                processMessage(it)
            }
        } catch (ex: Exception) {
            exit(ex)
        }
    }

    private fun processMessage(message:String){
        ExchangeStore.put(message)
    }
//db.getCollection('in_СозданиеПодстанции').find({ createdAt: { $gt: ISODate("2022-10-13T04:37:31.050Z") } })
    private  fun exit(exception: Exception){
        Logger.status("error")
        ExchangeStatusStore.setProcessorStatusError()
        ExchangeStatusStore.setProcessorErrorTime()
        exception.printStackTrace()
        Logger.traceBeg("exit")
        exitProcess(1)
    }





}