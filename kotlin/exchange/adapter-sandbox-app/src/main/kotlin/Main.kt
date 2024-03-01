package cc.datafabric.adapter.sandbox.app

import ProfileLoaderCsv
import cc.datafabric.adapter.lib.sys.Config


fun main(args: Array<String>) {
    Config.set("adp_timer_trace_enabled","false")
//    PipelineConverter.convertAll()
//    ProfileLoader.loadProfile()
//    val loadId = "Load 1"
//    MongoDbLoader.main(loadId)
//    ExchangeProcessor.processModelInput(loadId,loadId)
//    CockroachLoader.main()
//    CockroachLoader.profile()
      ProfileLoaderCsv.exportToCsv()
}





//@Throws(XMLStreamException::class, IOException::class)



