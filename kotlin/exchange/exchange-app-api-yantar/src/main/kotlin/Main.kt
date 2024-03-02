package cc.datafabric.exchange.app.api.yantar

import cc.datafabric.exchange.lib.Config
import cc.datafabric.exchange.lib.ConfigNames
import cc.datafabric.exchange.lib.Logger
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.UpdateOptions
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.Startup
import io.quarkus.runtime.StartupEvent
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.enterprise.event.Observes
import javax.inject.Singleton
import javax.ws.rs.POST
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Singleton
@Startup
@Path("/")
class Main {
    @GET
    @Path("test")
    fun getScriptParams(): Response {
        return Response.ok("yep").build()
    }
}








