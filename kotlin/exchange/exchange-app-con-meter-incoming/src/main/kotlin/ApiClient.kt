package cc.datafabric.exchange.app.con.meter.incoming


import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLContexts
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.apache.http.conn.ssl.NoopHostnameVerifier
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ApiClient {

    private val httpClient by lazy {
        createHttpClient()
    }

    private fun createHttpClient(): CloseableHttpClient {
        // https://www.baeldung.com/httpclient-ssl
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
        val builder = HttpClients.custom()
            .setSSLSocketFactory(sslsf)
            .setConnectionManager(connectionManager)

        val credentialsProvider = BasicCredentialsProvider()
        val user = Config.get(ConfigNames.apiUser)
        val password = Config.get(ConfigNames.apiPassword)
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(user, password))
        builder.setDefaultCredentialsProvider(credentialsProvider)
        return builder.build()
    }


    fun getUsagePoints(start: Date, end: Date, offset: Int, limit:Int, isSdp:Boolean): String {
        val body = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:abs="http://iec.ch/TC57/2011/abstract" xmlns:mes="http://iec.ch/TC57/2011/schema/message" xmlns:get="http://iec.ch/TC57/2011/podis15/GetCustomerConfig#" xmlns:get1="http://iec.ch/TC57/2011/podis15/GetUsagePointConfig#" xmlns:get2="http://iec.ch/TC57/2011/podis15/GetMeterConfig#" xmlns:get3="http://iec.ch/TC57/2011/podis15/GetMeterReadings#" xmlns:get4="http://iec.ch/TC57/2011/podis15/GetEndDeviceEvents#" xmlns:get5="http://iec.ch/TC57/2011/podis15/GetEndDeviceControl#" xmlns:end="http://iec.ch/TC57/2011/schema/podis/EndDeviceControls#" xmlns:com="http://iec.ch/TC57/2011/schema/podis/Common#">
           <soapenv:Header/>
           <soapenv:Body>
              <abs:Request>
                 <abs:message>
                    <mes:Header>
                       <mes:Verb>get</mes:Verb>
                       <mes:Noun>UsagePointConfig</mes:Noun>
                       <mes:Revision>14</mes:Revision>
                       <mes:Timestamp>${dateToString(getCurrentUtcDateTime())}</mes:Timestamp>
                       <mes:Source>Ext</mes:Source>
                       <mes:MessageID>${UUID.randomUUID()}</mes:MessageID>
                    </mes:Header>
                    <mes:Request>
                       <mes:Option>
                          <mes:name>limit</mes:name>
                          <mes:value>$limit</mes:value>
                       </mes:Option>
                       <mes:Option>
                          <mes:name>offset</mes:name>
                          <mes:value>$offset</mes:value>
                       </mes:Option>
                       <get1:GetUsagePointConfig>
                          <get1:isSdp>$isSdp</get1:isSdp>
                          <get1:TimeSchedule>
                             <get1:scheduleInterval>
                                <get1:end>${dateToString(end)}</get1:end>
                                <get1:start>${dateToString(start)}</get1:start>
                             </get1:scheduleInterval>
                          </get1:TimeSchedule>
                       </get1:GetUsagePointConfig>
                    </mes:Request>
                 </abs:message>
              </abs:Request>
           </soapenv:Body>
        </soapenv:Envelope>
        
    """.trimIndent()

        return  executeQuery(body)
    }




    fun getMeters(start: Date, end: Date, offset: Int, limit:Int): String {
        val body = """
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:abs="http://iec.ch/TC57/2011/abstract" xmlns:mes="http://iec.ch/TC57/2011/schema/message" xmlns:get="http://iec.ch/TC57/2011/podis15/GetCustomerConfig#" xmlns:get1="http://iec.ch/TC57/2011/podis15/GetUsagePointConfig#" xmlns:get2="http://iec.ch/TC57/2011/podis15/GetMeterConfig#" xmlns:get3="http://iec.ch/TC57/2011/podis15/GetMeterReadings#" xmlns:get4="http://iec.ch/TC57/2011/podis15/GetEndDeviceEvents#" xmlns:get5="http://iec.ch/TC57/2011/podis15/GetEndDeviceControl#" xmlns:end="http://iec.ch/TC57/2011/schema/podis/EndDeviceControls#" xmlns:com="http://iec.ch/TC57/2011/schema/podis/Common#">
   <soapenv:Header/>
   <soapenv:Body>
      <abs:Request>
         <abs:message>
            <mes:Header>
               <mes:Verb>get</mes:Verb>
               <mes:Noun>MeterConfig</mes:Noun>
               <mes:Revision>14</mes:Revision>
               <mes:Timestamp>${dateToString(getCurrentUtcDateTime())}</mes:Timestamp>
               <mes:Source>Ext</mes:Source>
               <mes:MessageID>${UUID.randomUUID()}</mes:MessageID>
            </mes:Header>
            <mes:Request>
               <mes:Option>
                  <mes:name>limit</mes:name>
                  <mes:value>$limit</mes:value>
               </mes:Option>
               <mes:Option>
                  <mes:name>offset</mes:name>
                  <mes:value>$offset</mes:value>
               </mes:Option>
               <get2:GetMeterConfig>
                  <get2:TimeSchedule>
                     <get2:scheduleInterval>
                        <get2:end>${dateToString(end)}</get2:end>
                        <get2:start>${dateToString(start)}</get2:start>
                     </get2:scheduleInterval>
                  </get2:TimeSchedule>
               </get2:GetMeterConfig>
            </mes:Request>
         </abs:message>
      </abs:Request>
   </soapenv:Body>
</soapenv:Envelope>
        
    """.trimIndent()

        return  executeQuery(body)
    }


    private fun executeQuery(body:String): String {
        val url = Config.get(ConfigNames.apiUrl)
        val request = HttpPost(url)
        request.entity = ByteArrayEntity(body.toByteArray())
        val response = httpClient.execute(request)
        val responseBody = EntityUtils.toString(response.entity!!)
        if (response.statusLine.statusCode >= 300) {
            val requestInfo = "request: POST $url\nbody:\n$body"
            val responseInfo = "response:" + response.statusLine.toString() + "\nbody:\n" + responseBody
            throw Exception("$requestInfo\n$responseInfo")
        }
        return responseBody
    }

    private fun getCurrentUtcDateTime(): Date {
        val zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
        return Date.from(zonedDateTime.toInstant())
    }
    private fun dateToString(value: Date): String? {
        val localOffsetDate = value.toInstant().atOffset(OffsetDateTime.now().offset)
        val utcOffsetDate = localOffsetDate.withOffsetSameInstant(ZoneOffset.UTC)
        return utcOffsetDate.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}