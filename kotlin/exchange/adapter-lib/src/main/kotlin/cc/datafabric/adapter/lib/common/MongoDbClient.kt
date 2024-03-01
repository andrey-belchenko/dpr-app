package cc.datafabric.adapter.lib.common

import cc.datafabric.adapter.lib.common.tools.Stopwatch
import cc.datafabric.adapter.lib.sys.*
import com.mongodb.*
import com.mongodb.client.MongoClients



object MongoDbClient {
    val instance by lazy {
        Stopwatch.start("mongo init")
        val uri = Config.get("adp_mongo_uri")
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .serverApi(serverApi)
            .build()
        val res =  MongoClients.create(settings)
        Stopwatch.stop("mongo init")
        res
    }
}
