package cc.datafabric.exchange.app.con.sk11.outgoing

import cc.datafabric.adapter.lib.rabbit.RabbitConsumer
import cc.datafabric.adapter.lib.sys.Config

fun main(args: Array<String>) {

    //для того чтобы использовать логирование от адаптеров
//    Config.set("adpName", Config.get(ConfigNames.adp_processor_name))
//    Config.set("adpMongoServDb", Config.get("adp_mongo_exchange_db"))
    App.main()

}






