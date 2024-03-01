package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Logger
import com.lordcodes.turtle.shellRun
import java.io.File
import java.util.*

object ExchangeSettingsCmd {

    private fun isWindows(): Boolean {
        Logger.traceData(System.getProperty("os.name"))
        return System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")
    }
    private fun run(commandName:String) {

        val npm = if (isWindows()) "npm.cmd" else "npm"
        shellRun(npm, listOf("run", commandName), File(ExchangeSettingsRepository.builderDir))
    }

    fun compile(){
        run("compile")
    }
}