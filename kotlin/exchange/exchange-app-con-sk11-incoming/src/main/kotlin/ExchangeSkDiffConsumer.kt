package cc.datafabric.exchange.app.con.sk11.incoming


import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.exchange.ExchangeDatabase
import cc.datafabric.adapter.lib.exchange.ExchangeErrorLogger
import cc.datafabric.adapter.lib.exchange.ExchangeMessageLogger
import cc.datafabric.adapter.lib.exchange.ExchangeStatusStore
import cc.datafabric.adapter.lib.sk.client.SkModelClient
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.gridfs.GridFSBuckets
import org.bson.Document
import org.bson.types.ObjectId
import java.io.InputStream
import java.util.*
import kotlin.system.exitProcess

object ExchangeSkDiffConsumer {


    private const val lastVersionVar = "lastVersion"
    private const val targetVersionVar = "targetVersion"
    private val timerInterval: Long = Config.get(ConfigNames.skConsumingIntervalMs).toLong()
    fun consume(diffProcessor: (DiffInfo) -> Unit) {
        ExchangeStatusStore.setProcessorStatusWaiting()
        startTimer(diffProcessor)
    }
    private val timer = Timer()
    private fun startTimer(diffProcessor: (DiffInfo) -> Unit) {
        Logger.traceFun {
            val timerTask = object : TimerTask() {
                override fun run() {
                    Logger.timerActionStarted()
                    if (!ExchangeStatusStore.getProcessorDisabled()){

                        action(diffProcessor)

                    }
                    startTimer(diffProcessor)
                    Logger.timerActionFinished()
                }
            }
            timer.schedule(timerTask, timerInterval)
        }
    }




    private fun action( diffProcessor: (DiffInfo) -> Unit) {
        Logger.traceFun {
            try{

                val coll = ExchangeDatabase.db.getCollection("sys_SkDiff")
                if (coll.countDocuments()!=0L){ // для импорта диффов при отладке

                    Logger.workStarted()
                    Logger.status("found diff for import")
                    ExchangeStatusStore.setProcessorStatusProcessing()
                    val gridFSBuckets = GridFSBuckets.create(ExchangeDatabase.db);
                    val doc = (coll.find().first() as Document)
                    val fileId = doc["diffFileId"] as ObjectId
                    val diff = gridFSBuckets.openDownloadStream(fileId)

                    val filerName =  doc["filter"].toString()
                    ExchangeMessageLogger.suppress()
                    diffProcessor(DiffInfo(null,diff,filerName,null,null,true))
                    ExchangeMessageLogger.resume()
                    ExchangeStatusStore.setProcessorCompletionTime()
                    coll.deleteMany(Document())
                    gridFSBuckets.delete(fileId)
                    Logger.workFinished()
                }else{
                    val lastVersion = ExchangeStatusStore.getInt(lastVersionVar)
                    if (lastVersion!=null){
                        var targetVersion = ExchangeStatusStore.getInt(targetVersionVar)
                        if (targetVersion==null){
                            targetVersion = SkModelClient.getActualModelVersion()
                            Logger.status("found actualVersion:$targetVersion")
                        }else{
                            Logger.status("found targetVersion preset:$targetVersion")
                        }
                        if (targetVersion!=lastVersion) {
                            Logger.workStarted()
                            ExchangeStatusStore.setProcessorStatusProcessing()
                            Logger.status("lastVersion:$lastVersion -> targetVersion:$targetVersion")
                            val diff = SkModelClient.getModelVersionsDifference(lastVersion, targetVersion)
                            // todo установка filterName тут - костыль
                            diffProcessor(DiffInfo(diff,diff.byteInputStream(),"sk11-incoming",lastVersion,targetVersion,false))
                            ExchangeStatusStore.set(lastVersionVar, targetVersion)
                            ExchangeStatusStore.setProcessorCompletionTime()
                            Logger.workFinished()
                        }else{
                            Logger.status("lastVersion and targetVersion is $lastVersion")
                            ExchangeStatusStore.setProcessorStatusWaiting()
                        }
                    }else{
                        Logger.status("lastVersion not found")
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
                ExchangeErrorLogger.log(ex.message,ex.stackTraceToString())
                exitProcess(1)
            }


        }

    }

    class DiffInfo (
        val diffString:String?,
        val diff:InputStream,
        val filterName:String,  // todo filterName тут - костыль
        val lastVersion:Int?,
        val targetVersion:Int?,
        val isImport:Boolean
        )

}