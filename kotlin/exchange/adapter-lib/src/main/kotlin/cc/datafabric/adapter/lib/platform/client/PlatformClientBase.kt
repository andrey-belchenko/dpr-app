package cc.datafabric.adapter.lib.platform.client

import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.Logger
import org.apache.commons.codec.binary.Base64
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.bson.Document
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import org.apache.http.entity.mime.MultipartEntity

open class PlatformClientBase {
    //Пробовал сгенерировать клиент через
    //https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
    //Не до конца понятно, как выполнять генерацию классов в существующем проекте
    //получается создать проект целиком или классы частично
    //странная проблема: не генерируется класс ImportDiffTaskRequest описанный в yaml, а если убрать ссылку на него ссылку в endpoint, то ок
    //ошибка cannot access class 'okhttp3.OkHttpClient'. Check your module classpath for missing or conflicting dependencies при попытке запуска
    //эксперименты с этим: коммит 96154b77 и соседние
    private val baseUrl = URL(Config.get(ConfigNames.platformApiUrl))



    protected fun sendRequestBase(request: HttpRequestBase, relativeUrl: String): String {
        return Logger.traceFun (relativeUrl) {
            queryToken()
            val url = URL(baseUrl, relativeUrl).toString()
            request.uri = URI(url)
            request.setHeader("Authorization", "Bearer $token")
            val client = HttpClients.createDefault()
            var response = client.execute(request)
            if (response.statusLine.statusCode==401){
                Logger.status("401 Unauthorized. Querying new token")
                token = null
                queryToken()
                request.setHeader("Authorization", "Bearer $token")
                response = client.execute(request)
            }
            val responseBody = EntityUtils.toString(response.entity!!,"utf-8")
            checkResponse(request,response)
            client.close()
            Logger.traceData(responseBody)
            return@traceFun responseBody
        }
    }


    private var token:String? = null


    private fun queryToken(){

        Logger.traceFun {
            if (token!=null){
                Logger.status("Token already acquired")
                return@traceFun
            }
            val request = HttpPost()
            val url =  Config.get(ConfigNames.keycloakUrl)
            request.uri = URI(url)
            request.setHeader("Content-Type", "application/x-www-form-urlencoded")
            //https://www.baeldung.com/httpclient-basic-authentication
            val auth = Config.get(ConfigNames.keycloakClientName) + ":" + Config.get(ConfigNames.keycloakClientPassword)
            val encodedAuth = Base64.encodeBase64(auth.toByteArray(StandardCharsets.ISO_8859_1))
            val authHeader = "Basic " + String(encodedAuth)
            request.setHeader("Authorization", authHeader)
            val params = mutableListOf<NameValuePair>()
            params.add(BasicNameValuePair("username", Config.get(ConfigNames.keycloakUserName)))
            params.add(BasicNameValuePair("password", Config.get(ConfigNames.keycloakUserPassword)))
            params.add(BasicNameValuePair("grant_type", "password"))
            request.entity = UrlEncodedFormEntity(params)
            val client = HttpClients.createDefault()
            val response = client.execute(request)
            checkResponse(request,response)
            val responseBody = EntityUtils.toString(response.entity!!,"utf-8")
            client.close()
            val doc = Document.parse(responseBody)
            token =  doc["access_token"].toString()

        }

    }



    private fun checkResponse(request: HttpRequestBase, response: CloseableHttpResponse) {
        if (response.statusLine.statusCode >= 300) {
            throw Exception(request.uri.toString() + " status:" + response.statusLine.toString()+ "\nRequest:\n" + requestToString(request)+"\nResponse\n" + EntityUtils.toString(response.entity!!,"utf-8"))
        }
    }

//    https://stackoverflow.com/questions/18744226/httprequestbase-how-to-print-the-request-with-all-its-data
    private  fun requestToString(request: HttpRequestBase): String? {
        val sb = StringBuilder()
        sb.append( request.method )
        sb.append(" " + request.uri + "\n")
        val headers = request.allHeaders.joinToString("\n") { h -> h.name + " : " + h.value }
        sb.appendLine(headers)

        if (request is HttpPost){
            val entity = request.entity

            try {
                sb.appendLine(EntityUtils.toString(request.entity))
            }catch (e:UnsupportedOperationException) {
                // Multipart form entity does not implement #getContent()
                // todo не разобрался как прочитать содержимое в этом случае, наверное нужно переходить на другую библиотеку
            }
        }
        return sb.toString()
    }
}