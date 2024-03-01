package cc.datafabric.adapter.lib.exchange

import java.io.File
import java.util.*

object SourceChangeDetector {
    private val dict = mutableMapOf<String, Long>()
    fun checkChanges(path: String): Boolean {
        val lastLoaded = dict[path] ?: 0
        var maxLastModified = 0L
        File(path).walk().forEach {
            val lastModified = it.lastModified()
            if (maxLastModified < lastModified) {
                maxLastModified = lastModified
            }

        }
        if (maxLastModified > lastLoaded) {
            dict[path] = maxLastModified
            return true
        }
        return false
    }
}