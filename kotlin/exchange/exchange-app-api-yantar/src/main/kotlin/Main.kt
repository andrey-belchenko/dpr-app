package cc.datafabric.exchange.app.api.yantar

import io.quarkus.runtime.Startup
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
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








