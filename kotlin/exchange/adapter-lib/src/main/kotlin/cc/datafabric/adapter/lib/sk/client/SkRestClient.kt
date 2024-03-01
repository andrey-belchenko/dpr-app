package cc.datafabric.adapter.lib.sk.client

import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.util.EntityUtils
import java.net.URL

object SkRestClient {

    private  val httpClient by lazy {
        SkHttpClient.createHttpClient(false)
    }

    fun post(relativeUrl: String, body: String): String {

        val baseUrl = URL(Config.get(ConfigNames.skUrl))
        val url = URL(baseUrl, relativeUrl).toString()
        val request = HttpPost(url)
        request.entity = ByteArrayEntity(body.toByteArray())
        request.setHeader(
            "Content-Type", "application/json"
        )
        val authVal = "Bearer ${SkHttpClient.getToken()}"
        request.setHeader("Authorization", authVal)
        val response = httpClient.execute(request)
        val responseBody = EntityUtils.toString(response.entity!!)
        if (response.statusLine.statusCode>=300){
            val requestInfo = "request: POST $url\nbody:\n$body"
            val responseInfo = "response:"+ response.statusLine.toString()+"\nbody:\n" +responseBody
            throw  Exception("$requestInfo\n$responseInfo")
        }
        return responseBody
    }
}