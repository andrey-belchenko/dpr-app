package cc.datafabric.adapter.lib.platform.client

import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.apache.http.client.methods.HttpGet
import java.net.URLEncoder

object PlatformClientProfiles: PlatformClientBase() {
    fun getProfile(): String {
        return Logger.traceFun {

            val profileId = URLEncoder.encode(Config.get("adpProfileIri"), "utf-8")
            return@traceFun sendRequestBase(HttpGet(), "profiles/$profileId")
        }
    }
}