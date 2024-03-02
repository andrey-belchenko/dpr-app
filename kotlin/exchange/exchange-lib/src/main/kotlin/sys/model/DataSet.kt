package cc.datafabric.linesapp.sys.model

import java.util.*

class DataSet {
    private val objectMap = mutableMapOf<String, ModelObject>()
    val objectMapByCode = mutableMapOf<String, ModelObject>()


    val objects: Iterable<ModelObject>
        get() = objectMap.values

    fun get(id: String): ModelObject? {
        return objectMap[id]
    }

    private val changedObjects =  mutableSetOf<ModelObject>()

    private var trackChanges = false
    fun startTrackChanges(){
        changedObjects.clear()
        trackChanges =true
    }
    fun getChangedObjects():Iterable<ModelObject>{
        return changedObjects
    }
    fun objectChanged(value: ModelObject){
        if (!trackChanges) return
        changedObjects.add(value)
    }

    fun getByCode(id: String?): ModelObject? {
        return objectMapByCode[id]
    }

    private fun add(obj: ModelObject) {
        obj.dataSet = this
        objectMap[obj.id] = obj
    }

    fun remove(obj: ModelObject) {
        remove(obj.id)
    }

    fun remove(objects: Iterable<ModelObject>) {
        objects.forEach {
            remove(it)
        }
    }

    fun remove(id: String) {
        val obj = get(id)
        if (obj != null) {
            ReflectionUtils.clearObjectLinks(obj)
            objectMap.remove(obj.id)
        }
    }

//    inline fun <reified T : ModelObject> create(): T {
//        val obj = ReflectionUtils.createObject<T>()
//        obj.id = UUID.randomUUID().toString()
//        add(obj)
//        return obj
//    }
//
//    fun create(className: String): ModelObject {
//        val obj = ReflectionUtils.createObject(className)
//        obj.id = UUID.randomUUID().toString()
//        add(obj)
//        return obj
//    }

    inline fun <reified T : ModelObject> getOrCreate(id: String): T {
        return getOrCreate(id, T::class.java.simpleName) as T
    }

    fun getOrCreate(id: String, className: String): ModelObject {
        var obj = get(id)
        if (obj == null) {
            obj = ReflectionUtils.createObject(className)
            obj.id = id
            add(obj)
        }
        return obj
    }

    inline fun <reified T : ModelObject> getOrCreateByCode(code: String): T {
        return getOrCreateByCode(code, T::class.java.simpleName) as T
    }




    fun getOrCreateByCode(code: String, className: String): ModelObject {
        var obj = getByCode(code)
        if (obj == null) {
            obj = ReflectionUtils.createObject(className)
            obj.id = UUID.randomUUID().toString()
            add(obj)
            obj.baseCode = code
        }
        return obj
    }


}