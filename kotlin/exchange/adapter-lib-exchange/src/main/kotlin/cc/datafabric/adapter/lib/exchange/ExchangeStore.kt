package cc.datafabric.adapter.lib.exchange

import org.bson.Document
import cc.datafabric.adapter.lib.exchange.general.IncomingInMessage
import java.util.*



object ExchangeStore {




    private val db by lazy {
        ExchangeDatabase.db
    }



    private  var isProcessing:Boolean = false
    fun put(message:String){
        if (!isProcessing){
            isProcessing = true
            ExchangeStatusStore.setProcessorStatusProcessing()
        }
        if (doMessageLogging){
            ExchangeMessageLogger.log(message, "in")
        }
        val inMessage = IncomingInMessage(message)
        put(inMessage)
//        val doc = Document(
//            mapOf(
//                Names.Fields.dtoId to inMessage.getRootId(),
//                Names.Fields.messageId to UUID.randomUUID().toString(),
//                Names.Fields.payload to inMessage.asObject(),
//            )
//        )
//        put("${Names.Prefixes.income}_${inMessage.getEventName()}", doc )
//        put(Names.Collections.incomingMessages,doc)
    }

    fun put(message:Document){
        val inMessage = IncomingInMessage(message)
        put(inMessage)
    }

    private fun put(inMessage: IncomingInMessage){
        val objectId =  inMessage.getRootId()
        val eventId = inMessage.getEventName()
        val doc = Document(
            mapOf(
                Names.Fields.objectId to objectId,
                Names.Fields.dtoId to "$eventId:$objectId",
                Names.Fields.eventId to eventId,
                Names.Fields.messageId to UUID.randomUUID().toString(),
                Names.Fields.payload to inMessage.asObject(),
            )
        )
        put(Names.Collections.incomingMessages,doc)
        put("${Names.Prefixes.income}_${inMessage.getEventName()}", doc )

    }

    var inProgress = false
    fun put(collectionName:String, obj:Document){
//        val name =collectionInfo
//        val changedAt = ExchangeTimestamp.now(db)

        insert(collectionName,obj)
    }

    fun get(collectionName: String):Iterable<Document> {
      return db.getCollection(collectionName).find(Document())
    }




    // todo пакетная вставка толком не проверена
    // todo вынести в отдельный класс

    private const val insertBatchSize = 1000

    // todo что то намудрил с этим таймером , периодически ошибка при попытке запуска
    // пока просто отключаю где это не нужно
    var useTimer = true
    var doMessageLogging = true
    private var insertMap = mutableMapOf<String, MutableList<Document>>()
    private var counter = 0

    private fun reset() {
        insertMap =  mutableMapOf<String, MutableList<Document>>()
        counter = 0
    }

//    fun getCount(collectionName: String):Int{
//       return  countMap[collectionName] ?: 0
//    }
    private fun insert(collectionName: String, obj: Document) {
        if (!insertMap.containsKey(collectionName)) {
            insertMap[collectionName] = mutableListOf()
        }
        insertMap[collectionName]!!.add(obj)

        if (doMessageLogging){
            ExchangeMessageLogger.log(obj, "out")
        }

        counter++
        if (counter == insertBatchSize) {
            if (timerTask!=null) {
                timerTask!!.cancel()
            }
            applyInsert()
        }else{
            startTimer()
        }
    }






    fun insertWithTimestamp(collectionName: String,documents:List<Document>){
        val newFlag = "new"
        documents.forEach {
            // в поле устанавливается значение "new" а потом апдейтится на текущую дату,
            //  может быть есть способ получше, чтобы установить changedAt для новых записей с временем>=созданию документа
            it[Names.Fields.changedAt] = newFlag
        }
        db.getCollection(collectionName).insertMany(documents)
        val filter = Document()
        filter[Names.Fields.changedAt] = newFlag
        val upd = Document(
            mapOf(
                "\$currentDate" to Document(Names.Fields.changedAt,true)
            )
        )
        db.getCollection(collectionName).updateMany(filter, upd)
    }


    fun applyInsert(){

        val processingInsertMap = insertMap
        reset()


        if (processingInsertMap.keys.contains(Names.Collections.incomingMessages)){
            insertWithTimestamp(Names.Collections.incomingMessages, processingInsertMap[Names.Collections.incomingMessages]!!)
            CollectionChangeInfoStore.set(Names.Collections.incomingMessages)
        }


        processingInsertMap.keys.parallelStream().forEach{collectionName->
//            IndexManager.create(collectionName,false, listOf( Names.Fields.changedAt))
            if (collectionName!=Names.Collections.incomingMessages) {
                insertWithTimestamp(collectionName, processingInsertMap[collectionName]!!)
                CollectionChangeInfoStore.set(collectionName)
            }
        }
        Thread.sleep(1) // химия, чтобы при отслеживании изменений не было пропусков записей
        CollectionChangeInfoStore.commit()
        if (!inProgress){
            if (processingInsertMap.any()){
                ExchangeStatusStore.setProcessorCompletionTime()
            }
            if (!insertMap.any()){
                ExchangeStatusStore.setProcessorStatusWaiting()
            }
        }

    }

    //todo thread safety?
    private val timer = Timer()
    var timerTask : TimerTask? = null
    private fun startTimer() {
        if (!useTimer){
            return
        }
        if (timerTask!=null) {
            timerTask!!.cancel()
        }
        timerTask = object : TimerTask() {
            override fun run() {
                timerTask = null
                applyInsert()
            }
        }
        timer.schedule(timerTask, 3000)
    }
}