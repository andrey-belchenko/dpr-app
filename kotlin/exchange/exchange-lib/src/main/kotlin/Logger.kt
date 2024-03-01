package cc.datafabric.exchange.lib


import org.slf4j.LoggerFactory


object Logger {

    private val logger = LoggerFactory.getLogger("")
    fun status(message: String) {
        logInfo("[status] $message")
    }

    //todo трюки чтобы сильно не замусоривать код трассировками, пытался через AOP не получилось, производительность и надежность и удобство для отладки под вопросом, последить

    private fun getCaller():StackTraceElement{
        var depth = 3
        val thread = Thread.currentThread()
        var tr = thread.stackTrace[depth]
        while (tr.methodName.endsWith("lambda-0") || tr.methodName.endsWith("default")){
            depth++
            tr = thread.stackTrace[depth]
        }
        return tr
    }

    fun traceFunBeg(info:String="") {
        if (Config.isTraceDisabled()) return
        val tr = getCaller()
        traceBeg("${tr.className}.${tr.methodName}($info)")
    }


    fun traceFunProgress(info:String="") {
        if (Config.isTraceDisabled()) return
        val tr = getCaller()
        traceProgress("${tr.className}.${tr.methodName}($info)")
    }

    fun traceFunEnd(info:String="") {
        if (Config.isTraceDisabled()) return
        val tr = getCaller()
        traceEnd("${tr.className}.${tr.methodName}($info)")
    }

    private fun parsToString(pars: Array<out Any>):String{
        if (pars.isEmpty()){
            return ""
        }
        val sb = StringBuilder()
        var q=""
        for (it in pars) {
            sb.append(q)
            sb.append(it)
            q=","
        }
        return sb.toString()
    }

    private var isWorkInProgress = false
    fun workStarted() {
        isWorkInProgress = true
    }

    fun workFinished() {
        isWorkInProgress = false
    }
    private var isTimerActionInProgress = false
    fun timerActionStarted() {
        isTimerActionInProgress = true
    }

    fun timerActionFinished() {
        isTimerActionInProgress = false
    }


   private fun isTraceDisabled():Boolean {
        if (Config.isTraceDisabled()) return true
        if (isTimerActionInProgress && !isWorkInProgress && Config.isTimerTraceDisabled()) return true
        return false
    }
    //todo применить где возможно вместо traceFunBeg traceFunEnd
    fun <T> traceFun(vararg parsInfo:Any= arrayOf(), action:()->T): T {
        if (isTraceDisabled()) return action()
        //todo проверить для разных случаев
        val tr =getCaller()
        val pars = parsToString(parsInfo)
        traceBeg("${tr.className}.${tr.methodName}($pars)")
        val res = action()
        traceEnd("${tr.className}.${tr.methodName}($pars)")
        return  res
    }

    fun <T> traceMassiveFun(vararg parsInfo:Any= arrayOf(), action:()->T): T {
        if (Config.isMassiveTraceDisabled()) return action()
        if (isTraceDisabled()) return action()
        //todo проверить для разных случаев
        val tr =getCaller()
        val pars = parsToString(parsInfo)
        traceBeg("${tr.className}.${tr.methodName}($pars)")
        val res = action()
        traceEnd("${tr.className}.${tr.methodName}($pars)")
        return  res
    }

    fun trace(actionInfo: String, action:()->Unit) {
        if (Config.isTraceDisabled()) {
            action()
            return
        }
        traceBeg(actionInfo)
        action()
        traceEnd(actionInfo)
    }

    fun traceBeg(action: String) {
        if (Config.isTraceDisabled()) return
        trace("${action.trim()} ...")
    }

    fun traceProgress(action: String) {
        if (Config.isTraceDisabled()) return
        trace("${action.trim()} ...")
    }
    fun traceEnd(action: String) {
        if (Config.isTraceDisabled()) return
        trace("${action.trim()} completed")
    }


    fun trace(message: String) {
        if (Config.isTraceDisabled()) return
        logInfo("[trace] $message")
    }


    fun traceData(value: String?) {

        if (Config.isTraceDataDisabled()) return
        if (isTraceDisabled()) return
        logInfo("[trace data]\n$value")
    }


    fun warn(value: String){
        putWarn(value)
    }

    private fun putInfo(message: String) {
        logger.info(message)
    }

    private fun putWarn(message: String) {
        logger.warn(message)
    }

    private fun logInfo(message: String) {
        putInfo(message)
    }

}