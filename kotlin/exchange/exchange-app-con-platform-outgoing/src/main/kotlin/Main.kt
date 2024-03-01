package cc.datafabric.exchange.app.con.platform.outgoing

import cc.datafabric.adapter.lib.common.ConfigNames
import cc.datafabric.adapter.lib.sys.Config

fun main(args: Array<String>) {
    Config.set("adpName",Config.get(ConfigNames.processorName))
    Config.set("adpTargetMongoUri",Config.get("adp_mongo_uri"))//todo костыль
    App.main()
}






