package cc.datafabric.exchange.app.con.sk11.incoming

import cc.datafabric.adapter.lib.sys.Config
import java.io.File

fun main(args: Array<String>) {

    //для того чтобы использовать логирование от адаптеров
//    Config.set("adpName", Config.get(ConfigNames.adp_processor_name))
//    Config.set("adpMongoServDb", Config.get("adp_mongo_exchange_db"))
    App.main()
//    val fileName ="C:\\Repos\\datafabric\\mrsk-configuration\\exchange-configuration\\tools\\other\\buffer\\diff2.xml"
//    val text =  File(fileName).bufferedReader().readText()
//
//    Loader.load(text)
}






