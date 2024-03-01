package cc.datafabric.adapter.lib.sk.client


import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.sys.*
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.ssl.SSLContexts
import org.apache.http.ssl.TrustStrategy
import org.apache.http.util.EntityUtils
import org.bson.Document

object SkHttpClient {




    private fun isKerberos():Boolean {
        return Config.get(ConfigNames.skAuthType) == "token-kerberos"
    }
    fun createHttpClient(withCreds:Boolean):CloseableHttpClient {
        //        https://www.baeldung.com/httpclient-ssl
        val acceptingTrustStrategy = TrustStrategy { cert, authType -> true }
        val sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        val sslsf = SSLConnectionSocketFactory(
            sslContext,
            NoopHostnameVerifier.INSTANCE
        )
        val socketFactoryRegistry: Registry<ConnectionSocketFactory> =
            RegistryBuilder.create<ConnectionSocketFactory?>()
                .register("https", sslsf)
                .register("http", PlainConnectionSocketFactory())
                .build()

        val connectionManager = BasicHttpClientConnectionManager(socketFactoryRegistry)
        val builder= HttpClients.custom()
            .setSSLSocketFactory(sslsf)
            .setConnectionManager(connectionManager)
        if (withCreds){
            val credentialsProvider = BasicCredentialsProvider()
            val user = Config.get(ConfigNames.skUser)
            val domain  =  Config.get(ConfigNames.skDomain)
            val password = Config.get(ConfigNames.skPassword)
            if (isKerberos()){

                Logger.status("using UsernamePasswordCredentials ($user,$password)")
                credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(user, password));
            }else{
                Logger.status("using NTCredentials ($user,$password),null,$domain")
                credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    NTCredentials(user,password, null, domain)
                )
            }

            builder.setDefaultCredentialsProvider(credentialsProvider)
        }
        return builder.build()
    }

//    private  val httpClient by lazy {
//         createHttpClient(true)
//    }


    private  var token:String? = null

    fun getToken():String {

        if (token == null) {
            queryToken()
        }
        // если токен устарел, при попытке его использования будет ошибка
        // приложение перезапустится оркестратором
        return token!!
    }

    private fun queryToken() {
        Logger.traceFun {
            val request = HttpPost(Config.get(ConfigNames.skAuthEndpoint))
            request.setHeader(
                "Content-Type", "application/json"
            )
            val response =   createHttpClient(true).execute(request)
            val respStr = EntityUtils.toString(response.entity!!)
            Logger.traceData(respStr)
            token =  Document.parse(respStr)["access_token"].toString()
        }
    }


}