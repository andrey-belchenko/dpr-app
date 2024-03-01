package cc.datafabric.adapter.lib.sk.client

import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import cc.datafabric.adapter.lib.common.ConfigNames
object SkClientExtension {

    private var targetVersionId: Int? = null
    fun SkModelClient.getTargetVersionId():Int {
        if (targetVersionId !=null) {
            return targetVersionId!!
        }
        queryTargetVersion()
        if (targetVersionId !=null) {
            return targetVersionId!!
        }
        createTargetVersion()
        return targetVersionId!!
    }

    private fun SkModelClient.queryTargetVersion() {
        Logger.traceFun {
            val versions = getModelVersions()
            val version = versions.children().firstOrNull() {
                it["Name"]?.getText() == Config.get(ConfigNames.skVersion)
                        && it["ModelVersionState"]?.getText() == "Open"
            }
            if (version != null) {
                targetVersionId = version["Id"]!!.getText().toInt()
            }
            Logger.traceData(targetVersionId.toString())
        }
    }

    private fun SkModelClient.createTargetVersion() {
        Logger.traceFun {
            val actualVersion = getActualModelVersion()
            targetVersionId = createModelVersion(actualVersion, Config.get(ConfigNames.skVersion))
            Logger.traceData(targetVersionId.toString())
        }
    }
}