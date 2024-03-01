package cc.datafabric.exchange.app.proc

import cc.datafabric.adapter.lib.exchange.ExchangeSettingsCmd
import cc.datafabric.adapter.lib.exchange.ExchangeStatusStore
import cc.datafabric.adapter.lib.sys.Config


fun main(args: Array<String>) {


//    ExchangeSettingsCmd.compile()
    Config.set("adpTargetMongoUri", Config.get("adp_mongo_uri"))//todo костыль
    App.main()
}






