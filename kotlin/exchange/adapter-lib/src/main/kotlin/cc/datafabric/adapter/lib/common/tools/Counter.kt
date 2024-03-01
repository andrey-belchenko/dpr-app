package cc.datafabric.adapter.lib.common.tools

import java.util.concurrent.ConcurrentHashMap


object Counter {
    private val scales = ConcurrentHashMap<String,Int>()
    fun inc(scaleName: String, count: Int = 1) {
        scales[scaleName] = (scales[scaleName] ?: 0) + count
    }

    override fun toString(): String {

        val sb= StringBuilder()
        scales.forEach{
            sb.appendLine("${it.key}: ${it.value}")
        }
        return sb.toString()
    }
}