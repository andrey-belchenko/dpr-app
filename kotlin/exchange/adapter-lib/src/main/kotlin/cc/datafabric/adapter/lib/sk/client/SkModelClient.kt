package cc.datafabric.adapter.lib.sk.client


import cc.datafabric.adapter.lib.data.Namespace
import cc.datafabric.adapter.lib.common.XmlElement
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.util.EntityUtils
import cc.datafabric.adapter.lib.sys.*
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.ssl.TrustStrategy
import org.apache.http.ssl.SSLContexts
import org.bson.Document
import cc.datafabric.adapter.lib.common.ConfigNames
object SkModelClient {


    private  val nsSdm= Namespace("sdm","http://monitel.com/CK11/Services/SDM")
    private  val nsCom= Namespace("com","http://iec.ch/TC57/2011/schema/message")
    private  val nsMes= Namespace("mes","http://monitel.com/CK11/Services/SDM/Messages")
    private  val nsArr= Namespace("arr","http://schemas.microsoft.com/2003/10/Serialization/Arrays")

    fun getProfiles():String {
        return Logger.traceFun(){
            val body = XmlElement.parse(
                """
                    <sdm:GetProfiles $nsSdm $nsCom $nsMes>
                         <sdm:req>
                            <com:Header>
                               <com:Noun>Profiles</com:Noun>
                               <com:Verb>get</com:Verb>
                            </com:Header>
                            <com:Request>
                            </com:Request>
                         </sdm:req>
                    </sdm:GetProfiles>
                """.trimIndent()
            )
            val response = post("GetProfiles", body)
            val diffText = response.descendants("Content").first().getText()
            Logger.traceData(diffText)
            return@traceFun diffText
        }
    }
    fun createModelVersion(baseModelVersionId:Int,name:String):Int {
        Logger.traceFunBeg()
        val body = XmlElement.parse(
            """
            <sdm:CreateModelVersion $nsSdm $nsCom $nsMes>
                <sdm:request>
                    <com:Header>
                        <com:Noun>ModelVersion</com:Noun>
                        <com:Verb>create</com:Verb>
                    </com:Header>
                    <com:Request>
                        <mes:BaseModelVersionId>$baseModelVersionId</mes:BaseModelVersionId>
                        <mes:Description></mes:Description>
                        <mes:InfModelUid>${Config.get(ConfigNames.skModelUid)}</mes:InfModelUid>
                        <mes:Name>$name</mes:Name>
                    </com:Request>
                </sdm:request>
            </sdm:CreateModelVersion>
        """.trimIndent()
        )
        val response = post("CreateModelVersion", body)
        val id = response.descendants("ModelVersionId").first().getText().toInt()
        Logger.traceFunEnd()
        return id
    }
    fun getActualModelVersion():Int {
        Logger.traceFunBeg()
        val body = XmlElement.parse(
            """
            <sdm:GetActualModelVersion $nsSdm $nsCom $nsMes>
                <sdm:request>
                    <com:Header>
                        <com:Noun>ModelVersion</com:Noun>
                        <com:Verb>get</com:Verb>
                    </com:Header>
                    <com:Request>
                        <mes:InfModelUid>${Config.get(ConfigNames.skModelUid)}</mes:InfModelUid>
                    </com:Request>
                </sdm:request>
            </sdm:GetActualModelVersion>
        """.trimIndent()
        )
        val response = post("GetActualModelVersion", body)
        val id =  response.descendants("ModelVersions").first()!!["Id"]!!.getText().toInt()
        Logger.traceFunEnd()
        return id
    }
    fun getModelVersions(): XmlElement {
        Logger.traceFunBeg()
        val body = XmlElement.parse(
            """
            <sdm:GetModelVersions  $nsSdm $nsCom $nsMes $nsArr>
                <sdm:request>
                    <com:Header>
                        <com:Noun>ModelVersion</com:Noun>
                        <com:Verb>get</com:Verb>
                    </com:Header>
                    <com:Request>
                        <mes:InfModelUid>${Config.get(ConfigNames.skModelUid)}</mes:InfModelUid>
                        <!-- <mes:Ids> НЕ РАБОТАЕТ, все равно возвращает все
                            <arr:int>36</arr:int>
                        </mes:Ids> -->
                    </com:Request>
                </sdm:request>
            </sdm:GetModelVersions>
        """.trimIndent()
        )
        val response = post("GetModelVersions", body)
        return response.descendants("ModelVersions").first()
    }
    fun changeObjects(modelVersion:Int, diff: String) {
        Logger.traceFunBeg()
        val body = XmlElement.parse(
            """
             <sdm:ChangeObjects  $nsSdm $nsCom $nsMes>
                <sdm:req>
                    <com:Header>
                        <com:Noun>Objects</com:Noun>
                        <com:Verb>change</com:Verb>
                    </com:Header>
                    <com:Payload>
                        <com:Format>CIMXML</com:Format>
                        <mes:Content>
                        </mes:Content>
                    </com:Payload>
                    <com:Request>
                        <mes:Source>
                            <mes:InfModelUid>${Config.get(ConfigNames.skModelUid)}</mes:InfModelUid>
                            <mes:ModelVersionId>$modelVersion</mes:ModelVersionId>
                        </mes:Source>
                    </com:Request>
                </sdm:req>
            </sdm:ChangeObjects>
        """.trimIndent()
        )
        body.descendants("Content").first().setCdata(diff)
        post("ChangeObjects", body)
        Logger.traceFunEnd()
    }
    fun getModelVersionsDifference(reverseMvId:Int, forwardMvId:Int):String {
        Logger.traceFunBeg()
        val modUid=Config.get(ConfigNames.skModelUid)
        val body = XmlElement.parse(
            """
        <sdm:GetModelVersionsDifference   $nsSdm $nsCom $nsMes>
            <sdm:request>
                <com:Header>
                    <com:Noun>ModelVersionsDifference</com:Noun>
                    <com:Verb>get</com:Verb>
                </com:Header>
                <com:Request>
                    <mes:ForvardInfModelUid>$modUid</mes:ForvardInfModelUid>
                    <mes:ForwardMvId>$forwardMvId</mes:ForwardMvId>
                    <mes:ReverseInfModelUid>$modUid</mes:ReverseInfModelUid>
                    <mes:ReverseMvId>$reverseMvId</mes:ReverseMvId>
                </com:Request>
            </sdm:request>
        </sdm:GetModelVersionsDifference>
        """.trimIndent()
        )
        val response = post("GetModelVersionsDifference", body)
        val diffText = response.descendants("Differences").first().getText()
        Logger.traceData(diffText)
        Logger.traceFunEnd()
        return  diffText
    }





    private fun isTokenAuthType():Boolean {
        return Config.get(ConfigNames.skAuthType).startsWith( "token")
    }



//    private  val httpClient by lazy {
//        if (isTokenAuthType()) {
//           SkHttpClient.createHttpClient(false)
//        } else {
//            SkHttpClient.createHttpClient(true)
//        }
//    }
    // MC-202
    private fun getHttpClient():CloseableHttpClient {
        if (isTokenAuthType()) {
          return  SkHttpClient.createHttpClient(false)
        } else {
          return  SkHttpClient.createHttpClient(true)
        }
    }

    private fun post(action: String, body: XmlElement): XmlElement {
        getHttpClient().use { httpClient ->
            val requestBody = prepareRequestBody(body)
            val request = prepareRequest(action, requestBody)
            val response = httpClient.execute (request)
            var respStr = EntityUtils.toString(response.entity!!)
            Logger.traceData(respStr)
            val respXml = try {
                val i = respStr.indexOf("<")
                if (i > 0) {
                    //в новой версии API бывает, что приходит с какой-то фигней в начале
                    respStr = respStr.removeRange(0, i)
                }
                parseResponse(respStr)
            } catch (_: Exception) {
                null
            }
            //todo корректность условий и производительность?
            //todo какой тип исключения лучше использовать?
            if (
                response.statusLine.statusCode >= 300 ||
                respXml == null ||
                respXml.descendants("Result").firstOrNull()?.getText() != "OK"
            ) {
                throw Exception("Sk11 returned error\nREQUEST\n${request}\nRESPONSE\n${response}\n$respStr")
            }
            return respXml
        }
    }

    private fun prepareRequestBody(body: XmlElement): String {
        return Logger.traceFun {
            val envelope = XmlElement.parse(
                """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                    <soapenv:Header />
                    <soapenv:Body>
                    </soapenv:Body>
                </soapenv:Envelope>
            """.trimIndent()
            )
            envelope["Body"]!!.add(body)
            val requestBody = envelope.toXmlString()
            Logger.traceData(requestBody)
            return@traceFun requestBody
        }
    }
    private fun prepareRequest(action: String, requestBody:String): HttpPost {
        return Logger.traceFun {
            val request = HttpPost(Config.get(ConfigNames.skEndpoint))
            request.entity = ByteArrayEntity(requestBody.toByteArray())
            request.setHeader(
                "Content-Type", "text/xml;charset=UTF-8"
            )
            request.setHeader("SOAPAction", "http://monitel.com/CK11/Services/SDM/SdmService/$action")
            if (isTokenAuthType()){
                val authVal = "Bearer ${SkHttpClient.getToken()}"
                request.setHeader("Authorization",authVal )
//                request.setHeader("Authorization","Bearer eTVnhVxwUap3kRagOnELCQis6jPu81JCwGt72tCUkCEvLegeLbTiWNXLTu9FfJKT-LC_YLtYBV_7ESumC24cfcYDJ4hME_JYHuQq_p8_viPVTfWB4qhe5t3uAyy855eFP6fIR8z_GBYfCqasW8TgokcafVGHzABLDV6ucWXjWU62q9ii2JkHaCnCVG0qVuy6Zyavsa3kGMP21PdviiVX8oACxGfJIsTg5obtUG5ISyS-T9OhHrHbZTPzNe1tyTvDAPdg5DLRpg-aVOBzKAq4SqIsOTnr49ZLpViB3L9DbQjqGaNgduLyJIFKhApxr7_eMAk4RvB7WcuqMyjhChM32g")
            }
            return@traceFun request
        }
    }
//    private fun sendRequest(request:HttpPost): CloseableHttpResponse {
//        return Logger.traceFun {
//            getHttpClient().use {
//                return@traceFun it.execute(request)
//            }
////            return@traceFun httpClient.execute(request)
//        }
//    }
    private fun parseResponse(value:String): XmlElement {
        //todo оценить производительность и устойчивость на больших diff
        return Logger.traceFun {
            return@traceFun XmlElement.parse(value)
        }
    }





}