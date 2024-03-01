package cc.datafabric.adapter.lib.mapping

import cc.datafabric.adapter.lib.data.*
import cc.datafabric.adapter.lib.rdf.*
import cc.datafabric.adapter.lib.sys.Logger

object DataMappingProcessor {
    var skipUnmapped= false
    fun convertDiff(diff: DataDiff, map: DataMap): DataDiff {
        return DataDiff(
            convertModel(diff.getForwardDiffModel(),map,isDelete = false),
            convertModel(diff.getReverseDiffModel(),map,isDelete = true)
        )
    }
    private fun convertModel(model: IDataModel?, map: DataMap, isDelete:Boolean): RdfModel? {
        if (model==null) {
            return null
        }
        val newModel = RdfFactory.createModel()


        model.getNamespaces().forEach {
            newModel.setNamespace(it)
        }



        val origEntitiesByNewId = mutableMapOf<String, IDataEntity>()
        val newEntityClasses = mutableMapOf<String, DataClass>()
        model.getEntities().forEach { origEnt ->
            val newEnt = processEntity(origEnt, map, isDelete, newModel,newEntityClasses)
            if (newEnt!=null){
                origEntitiesByNewId[newEnt.getId()] = origEnt
            }
        }

        newModel.getEntities().forEach {newEnt->
            val origEnt = origEntitiesByNewId[newEnt.getId()]
            origEnt!!.getProperties().forEach { origProp ->
                origProp.value.forEach {origPropValue->
                    processProperty(origPropValue, map, newEnt,newEntityClasses)
                }
            }
        }
        return newModel
    }

    private fun processEntity(
        origEnt: IDataEntity,
        map: DataMap,
        isDelete: Boolean,
        newModel: IDataModel,
        newEntityClasses:MutableMap<String, DataClass>
    ): IDataEntity? {
        //TODO упростить , прокомментировать логику

//        if (origEnt.getUri()=="_eaa7818b-a5f0-41fe-a3d3-ee1c56143364"){
//            val x =0
//        }
        val origClass = origEnt.getClass()
        var newClass = origClass
        if (map.hasPropertyMap()) {
            if (origClass==null){
                throw  Exception("Can't determine class for entity ${origEnt.getId()}")
            }
            newClass = map.getMappedClass(origClass.name)
            if (newClass == null) {
                return null
            }
        }



        var newUri = origEnt.getUri()
        if (map.hasKeyMap()) {
            val origKey = origEnt.getId()
            val newKeyInfo =
                if (origEnt.isChanged() || isDelete) {
                    // если reverseDiff и есть класс (isChanged определяется отсутствию класса) значит объект удаляется, иначе это просто ссылка
                    val deleteKey =isDelete && !origEnt.isChanged()
                    map.getMappedKey(origKey,deleteKey)
                } else {
                    map.getMappedKey(origClass!!, newClass!!, origKey)
                }
            if (newKeyInfo == null) {

                val message = "Can't find mapped key for id:$origKey"

                if (skipUnmapped) {
                    Logger.warn(message)
                    return null
                } else {
                    throw Exception(message)
                }

            }
            //todo это скорее всего не нужно
            if (newClass == null) {
                newClass = DataClass(newKeyInfo.classIri)
            }
            newUri = newClass.namespace.uri + newKeyInfo.key
            newEntityClasses[newKeyInfo.key] = newClass
            if (
                origEnt.isChanged()
//                || (!isDelete && !newKeyInfo.isGenerated!!)
            ) {
                newClass = null // У измененных записей не д. быть класса
            }
        }
        return newModel.createEntity(newUri, newClass)
    }
    private fun processProperty(
        origPropValue: IDataPropertyValue,
        map: DataMap,
        newEntity: IDataEntity,
        newEntityClasses:MutableMap<String, DataClass>
    ) {
        //TODO упростить , прокомментировать логику
        val origProp = origPropValue.getProperty()
        val newProps= mutableListOf<DataProperty>()
//        val newProp:RdfProperty? = origProp
        if (map.hasPropertyMap()) {
            val newClass = newEntityClasses[newEntity.getId()]
            val mappedProps = map.getMappedProperty(newClass!!.name,origProp.getIri()) ?: return
            newProps.addAll(
                mappedProps.map {
                    DataProperty(it.namespace,it.name,origProp.isReference)
                }
            )
        }else{
            newProps.add(origProp)
        }

        var newValue = origPropValue.getValueText()
        if (map.hasKeyMap() && origPropValue is IDataPropertyValueResource) {
            val origKey = origPropValue.getValueText()


            var newKey = map.getMappedKey(origKey,false)?.key
            val dataClass =  origPropValue.getValueClass()

            // не правильно, но более стабильный вариант,
            // получается создание незамапленного объекта
            if (newKey==null && dataClass!=null) {
                newKey = map.getMappedKey(dataClass, dataClass, origKey)?.key
            }else{
                if (newKey==null){
                    newKey = map.getMappedKey(origKey,false)?.key
                }
            }

            if (newKey != null) {
                newValue = newKey
            } else {
                val message = "Can't find mapped key for id:$origKey"
                if (skipUnmapped) {
                    Logger.warn(message)
                    return
                } else {
                    throw Exception(message)
                }
            }
        }

        newProps.forEach {newProp->
            newEntity.addPropertyValue(newProp, newValue)
        }

    }




}