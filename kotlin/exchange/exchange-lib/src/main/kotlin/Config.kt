package cc.datafabric.exchange.lib

object Config {

    private val cache= mutableMapOf<String,String?>()

    private var isTraceEnabled=true
    private var isTraceDataEnabled=true
    private var isMassiveTraceEnabled=true
    private var isTimerTraceEnabled=true

    // todo перемудрил с инициализацией и хранением этих флагов, упростить
    init {

        try {
            get("adp_trace_enabled")
            get("adp_trace_data_enabled")
            get("adp_timer_trace_enabled")
            get("adp_massive_trace_enabled")
        } catch (e: Exception) {

        }
    }

    fun isMassiveTraceDisabled():Boolean{
        return !isMassiveTraceEnabled
    }

    fun isTimerTraceDisabled():Boolean{
        return !isTimerTraceEnabled
    }

    fun isTraceDisabled():Boolean{
        return !isTraceEnabled
    }

    fun isTraceDataDisabled():Boolean{
        return !isTraceDataEnabled
    }

    fun getArray(name: String): Iterable< String> {
         val value = get (name)
         return value.split(",").map { it.trim() }
    }

    fun get(name: String): String {
        return tryGet(name)!!
    }

    fun tryGet(name: String): String? {
        if (!cache.containsKey(name)){

            set(name,read(name),doLogging = false)
        }
        return cache[name]
    }

    private fun read(name: String):String?{
        return Logger.traceFun (name){
            val value = System.getenv(name)
            Logger.traceData(value)
            return@traceFun value
        }
    }

    fun set (name:String , value:String){
        set(name,value, doLogging = true)
    }
    private fun set (name:String , value:String?, doLogging:Boolean){
        if (doLogging){
            Logger.traceFunBeg(name)
            Logger.traceData(value)
        }
        cache[name]=value
        when (name) {
            "adp_trace_enabled" -> isTraceEnabled= get(name) == "true"
            "adp_massive_trace_enabled" -> isMassiveTraceEnabled = get(name) == "true"
            "adp_trace_data_enabled" -> isTraceDataEnabled= get(name) == "true"
            "adp_timer_trace_enabled" -> isTimerTraceEnabled= get(name) == "true"
        }
        if (doLogging){
            Logger.traceFunEnd(name)
        }
    }
}