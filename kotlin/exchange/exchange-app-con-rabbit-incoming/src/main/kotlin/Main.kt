package cc.datafabric.exchange.app.con.rabbit.incoming

import cc.datafabric.adapter.lib.rabbit.RabbitConsumer
import cc.datafabric.adapter.lib.sys.Config

fun main(args: Array<String>) {

    Config.set("adpTargetMongoUri",Config.get("adp_mongo_uri"))//todo костыль
    App.main()
}






