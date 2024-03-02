package cc.datafabric.linesapp.sys.model

open class ModelObject {

    lateinit var id: String
    lateinit var dataSet: DataSet
    var code: String? = null
        get() = field
        set(value) {
            field =value
            baseCode = value
        }

    var baseCode: String? = null
        get() = field
        set(value) {
            if (field != value) {
                if (field != null) {
                    dataSet.objectMapByCode.remove(field)
                }
                field = value
                if (value != null) {
                    dataSet.objectMapByCode[value] = this
                }
            }

        }

    fun setPropertyValue(fieldName: String, value: Any?): Boolean {
        return ReflectionUtils.setPropertyValue(this, fieldName, value)
    }

    fun addLink(fieldName: String, value: ModelObject): Boolean {
        return ReflectionUtils.addLink(this, fieldName, value)
    }

    fun changed(){
        this.dataSet.objectChanged(this)
    }

    val extraProperties = mutableMapOf<String,Any?>()
    fun setExtraProperty(name:String,value: Any?){
        extraProperties[name] = value
    }
    fun getExtraProperty(name: String):Any?{
        return  extraProperties[name]
    }




}
