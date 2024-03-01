package cc.datafabric.adapter.lib.exchange.common
import cc.datafabric.adapter.lib.sys.Config
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap


object Stopwatch {

    private class Scale {
        var fixedValue: Long = 0
        var count: Int = 0
        var startTime: Instant? = null
        var runingCount: Int = 0
        fun getValue(): Long {
            var current = 0L
            if (startTime != null) {
                val now = Instant.now()
                current = ChronoUnit.MICROS.between(startTime, now)
            }
            return fixedValue + current
        }
    }

    private val scales = ConcurrentHashMap<String, Scale>()

    fun start(scaleName: String) {
        if (!scales.containsKey(scaleName)) {
            scales[scaleName] = Scale()
        }
        val scale = scales[scaleName]!!
        scale.startTime = Instant.now()
        scale.runingCount++
        scale.count++
    }

    fun stop(scaleName: String) {
        val scale = scales[scaleName]!!
        scale.runingCount--
        if (scale.runingCount==0){
            scale.fixedValue = scale.getValue()
            scale.startTime = null
        }
    }

    fun reset(scaleName: String) {
        scales.remove(scaleName)
    }

    fun reset() {
        scales.clear()
    }

    fun get(scaleName: String):Long? {
        return scales[scaleName]?.getValue()
    }


    // не проверено после изменения
    fun <T> measure(scaleName:String, action:()->T): T {
        start(scaleName)
        val res = action()
        stop(scaleName)
        return res
    }


    fun <T> measureFun(info:String="", action:()->T): T {
        if (Config.isTraceDisabled()) return action()
        // наличие фактического параметра влияет на глубину стека
        var stackDepth = 2
        if (info == "") {
            stackDepth = 3
        }
        val tr = Thread.currentThread().stackTrace[stackDepth]
        val scaleName = "${tr.className}.${tr.methodName}($info)"
        start(scaleName)
        val res = action()
        stop(scaleName)
        return res
    }

    override fun toString(): String {
        val sb = StringBuilder()
        scales.forEach {
            val obj = it.value
            val value = (obj.getValue().toDouble() / 1000).toLong()
            sb.appendLine("${it.key}: $value ms / ${obj.count}")
        }
        expressions.forEach {
            val obj = it.value
            val value = (obj.getValue().toDouble() / 1000).toLong()
            sb.appendLine("${it.key}: $value ms")
        }
        return sb.toString()
    }

    private class Expression (val addScales:Iterable<String>, val subtractScales:Iterable<String> ){
        fun getValue():Long {
            val add = addScales.sumOf { scales[it]?.getValue() ?: 0 }
            val subtract = subtractScales.sumOf { scales[it]?.getValue() ?: 0 }
            return add - subtract
        }
    }

    private val expressions = ConcurrentHashMap<String, Expression>()
    fun addExpression(name:String ,addScales:Iterable<String>, subtractScales:Iterable<String> = listOf() ) {
        expressions[name] = Expression(addScales, subtractScales)
    }


}