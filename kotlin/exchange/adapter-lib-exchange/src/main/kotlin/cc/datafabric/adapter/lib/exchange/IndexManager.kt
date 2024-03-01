package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document

object IndexManager {
    //todo mongo не создает индекс повторно если он уже существует, но нужно доработать чтобы минимизировать количество обращений к БД

    private var enabled = false
    fun enable(){
        enabled = true
    }
    private val createdIndexes = mutableSetOf<String>()


    private  class  IndexInfo (val collectionName:String, val isUnique:Boolean, val fieldNames:Iterable<String>)
    private  var definedIndexes = mutableListOf<IndexInfo>()
    fun create(collectionName:String, isUnique:Boolean, fieldNames:Iterable<String>, doCheck:Boolean = true){
        if (!enabled) return
        val indexId = collectionName+"-"+fieldNames.joinToString ("-")
        if (doCheck && createdIndexes.contains(indexId)){
            return
        }
        val col = ExchangeDatabase.getCollection(collectionName)
        col.createIndex(
            Indexes.ascending(fieldNames.toList()),
            IndexOptions().unique(isUnique)
        )
        createdIndexes.add(indexId)
    }

    fun create(collectionName:String, isUnique:Boolean, fieldNames:Iterable<String>) {
        createIndex(ExchangeDatabase.db, collectionName, isUnique, fieldNames, true)
    }

    fun create(database:MongoDatabase,collectionName:String, isUnique:Boolean, fieldNames:Iterable<String>) {
        createIndex(database, collectionName, isUnique, fieldNames, false)
    }

    private fun createIndex( database:MongoDatabase,collectionName:String, isUnique:Boolean, fieldNames:Iterable<String>, doCheck:Boolean = true){
        if (!enabled) return
        val indexId = collectionName+"-"+fieldNames.joinToString ("-")
        if (doCheck && createdIndexes.contains(indexId)){
            return
        }
        val col = database.getCollection(collectionName)
        col.createIndex(
            Indexes.ascending(fieldNames.toList()),
            IndexOptions().unique(isUnique)
        )
        if (doCheck){
            createdIndexes.add(indexId)
        }
    }


    fun define(collectionName:String, isUnique:Boolean, fieldNames:Iterable<String>){
        if (!enabled) return
        val indexId = collectionName+"-"+fieldNames.joinToString ("-")
        if (createdIndexes.contains(indexId)){
            return
        }
        definedIndexes.add(IndexInfo(collectionName, isUnique, fieldNames))

    }


    private var creating = false
    fun createDefined(){
        // todo наверное это криво , разобраться с thread safety сделать нормально, выскакивала ConcurrentModificationException
        while (creating){
            Thread.sleep(100)
        }
        creating =true
        val indexesInProgress = definedIndexes
        definedIndexes =  mutableListOf()
        Logger.traceFun {
            indexesInProgress.parallelStream().forEach {
                create(it.collectionName,it.isUnique,it.fieldNames)
            }
        }
        creating = false
    }

    fun reset(){
        if (!enabled) return
        definedIndexes.clear()
        createdIndexes.clear()
        createSystemIndexes()
        ExchangeSettingsRepository.createIndexes()

    }

    private fun createSystemIndex(collectionName:String, isUnique:Boolean, fieldNames:Iterable<String>){
        create(collectionName,isUnique,fieldNames)
    }
    private fun createSystemIndexes(){


        createSystemIndex(Names.Collections.modelInput, false, listOf(Names.Fields.operationId))
        createSystemIndex(Names.Collections.modelEntitiesInput, false, listOf(Names.Fields.operationId, Names.Fields.idSource))
        createSystemIndex(Names.Collections.modelLinksInput, false, listOf(Names.Fields.operationId, Names.Fields.idSource))
        createSystemIndex(Names.Collections.modelFieldsInput, false, listOf(Names.Fields.operationId, Names.Fields.idSource))

        createSystemIndex(Names.Collections.modelImport, false, listOf(Names.Fields.changedAt))

        createSystemIndex(Names.Collections.modelEntities, false, listOf(Names.Fields.id))
        createSystemIndex(Names.Collections.modelEntities, true, listOf(Names.Fields.initialId))
        createSystemIndex(Names.Collections.modelEntities, false, listOf(Names.Fields.operationId))
        createSystemIndex(Names.Collections.modelEntities, false, listOf(Names.Fields.createdAt))
        createSystemIndex(Names.Collections.modelEntities, false, listOf(Names.Fields.changedAt))
        createSystemIndex(Names.Collections.modelEntities, false, listOf(Names.Fields.deletedAt))
//        createSystemIndex(Names.Collections.modelEntities, false, listOf(Names.Fields.typeUpdated))

        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.operationId))
        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.createdAt))
        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.changedAt))
        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.deletedAt))
        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.toId))
        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.fromId))
        createSystemIndex(Names.Collections.modelLinks, false, listOf(Names.Fields.linkId))
        createSystemIndex(Names.Collections.modelLinks, true, listOf(Names.Fields.fromId,Names.Fields.predicate))

        createSystemIndex(Names.Collections.modelFields, false, listOf(Names.Fields.operationId))
        createSystemIndex(Names.Collections.modelFields, false, listOf(Names.Fields.createdAt))
        createSystemIndex(Names.Collections.modelFields, false, listOf(Names.Fields.changedAt))
        createSystemIndex(Names.Collections.modelFields, false, listOf(Names.Fields.deletedAt))
        createSystemIndex(Names.Collections.modelFields, false, listOf(Names.Fields.id))
        createSystemIndex(Names.Collections.modelFields, true, listOf(Names.Fields.id,Names.Fields.predicate))


        createSystemIndex(Names.Collections.platformInput, false, listOf(Names.Fields.changedAt))

        createSystemIndex(Names.Collections.collectionChangeInfo,true,listOf(Names.Fields.collectionName))
        createSystemIndex(Names.Collections.collectionChangeInfo,false,listOf(Names.Fields.changedAt))
        createSystemIndex(Names.Collections.processorCollectionTimestamp,false,listOf(Names.Fields.processorName,Names.Fields.collectionName))
        createSystemIndex(Names.Collections.processorTimestamp, false, listOf(Names.Fields.processorName))
        createSystemIndex(Names.Collections.messageLog, false, listOf(Names.Fields.changedAt))
        createSystemIndex(Names.Collections.pipelineLog, false, listOf(Names.Fields.changedAt))

        createSystemIndex(Names.Collections.blockedDtoEntities, false, listOf(Names.Fields.entityId))
        createSystemIndex(Names.Collections.blockedDtoEntities, false, listOf(Names.Fields.dtoId))

        createSystemIndex(Names.Collections.unblockedDtoEntities, false, listOf(Names.Fields.entityId))
        createSystemIndex(Names.Collections.unblockedDtoEntities, false, listOf(Names.Fields.dtoId))
        createSystemIndex(Names.Collections.incomingMessages, false, listOf(Names.Fields.dtoId))
        createSystemIndex(Names.Collections.incomingMessages, false, listOf(Names.Fields.messageId))
        createSystemIndex(Names.Collections.incomingMessages, false, listOf(Names.Fields.eventId))
        createProcessorIndexesForCollectionWithId(Names.Collections.blockedDto)
        createSystemIndex(Names.Collections.blockedDto, false, listOf(Names.Fields.lastMessageId))

        createProcessorIndexesForCollectionWithId(Names.Collections.blockedMessages)
        createProcessorIndexesForCollectionWithId(Names.Collections.blockedDtoEntities)
        createProcessorIndexesForCollectionWithId(Names.Collections.blockedEntities)
        createProcessorIndexesForCollection(Names.Collections.messageInput)
        createSystemIndex(Names.Collections.forbiddenEntities, true, listOf(Names.Fields.fullId))
        createSystemIndex(Names.Collections.extraIdMatching, true, listOf(Names.Fields.fullId))
        createSystemIndex(Names.Collections.extraIdMatching, false, listOf(Names.Fields.platformId))

//        createSystemIndex(Names.Collections.extraIdMatching, true, listOf(Names.Fields.idSource,Names.Fields.id))

//        createSystemIndex(Names.Collections.processorCommand, false, listOf(Names.Fields.changedAt))
    }

    private fun createProcessorIndexesForCollection(collectionName:String){
        createSystemIndex(collectionName, false, listOf(Names.Fields.operationId))
        createSystemIndex(collectionName, false, listOf(Names.Fields.batchId))
        createSystemIndex(collectionName, false, listOf(Names.Fields.changedAt))
    }

    private fun createProcessorIndexesForCollectionWithId(collectionName:String){
        createProcessorIndexesForCollection(collectionName)
        createSystemIndex(collectionName, true, listOf(Names.Fields.id))
    }

}