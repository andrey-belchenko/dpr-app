package cc.datafabric.exchange.app.mail


import cc.datafabric.adapter.lib.exchange.*
import cc.datafabric.adapter.lib.sys.Config
import cc.datafabric.adapter.lib.sys.ConfigNames
import cc.datafabric.adapter.lib.sys.Logger
import org.bson.Document
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object App {


    fun main() {
        val collectionName = "out_notifications"
        ExchangeStatusStore.setProcessorStatusWaiting()
        ExchangeListener.listenCollections(listOf(collectionName)) { res ->
            res.changedCollections.forEach { col ->
                val docs = col.getDocuments().toList()
                docs.forEach { document ->
                    if (document[Names.Fields.isSent] != true) {
                        sendMail(document)
                        val collection = ExchangeDatabase.db.getCollection(collectionName)
                        val doc = Document(
                            mapOf(
                                Names.Fields.isSent to true,
                                Names.Fields.sendingId to UUID.randomUUID().toString()
                            )
                        )
                        val filer = Document("_id",  document["_id"])

                        collection.updateOne(
                            filer,
                            Document("\$set", doc)
                        )
                    }
                }
            }
        }
    }

    private fun sendMail(item: Document) {
        val recipients = (item["recipients"] as List<*>).joinToString(",")
        val props = Properties()

        props["mail.smtp.auth"] = "true"
        props["mail.smtp.host"] = Config.get(ConfigNames.mailSmtpHost)
        props["mail.smtp.port"] = Config.get(ConfigNames.mailSmtpPort)

        if (Config.get(ConfigNames.mailUseSsl) == "true") {
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.ssl.enable"] = "true"
            props["mail.smtp.ssl.protocols"] = "TLSv1.2"
        }

        val session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        Config.get(ConfigNames.mailLogin),
                        Config.get(ConfigNames.mailPassword)
                    )
                }
            })
        session.debug = true
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(Config.get(ConfigNames.mailSender)))
        message.setRecipients(Message.RecipientType.TO, recipients)
        message.subject = item["subject"].toString()
        val body = StringBuilder()
        body.appendLine("<html><body>")
        body.appendLine(item["body"]?.toString()?.replace(Names.Values.uiUrlVar, Config.get(ConfigNames.uiUrl)))
        body.appendLine("</body></html>")
        val htmlContent = body.toString()
        message.setContent(htmlContent, "text/html; charset=utf-8")
        Transport.send(message)
        Logger.status("$recipients ${message.subject}")
    }
}