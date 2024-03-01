package cc.datafabric.adapter.sandbox.app
import cc.datafabric.adapter.lib.sys.Logger
import com.lordcodes.turtle.shellRun
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object CockroachUserfile {
    fun uploadUserfileToCockroachDb(filePath: Path){
        Logger.traceFun {
            val inputFile=filePath.absolutePathString()
            val destination="import/${filePath.fileName.toString()}"
            val extraParams= listOf("--url","postgres://root:@cockroach.astu.lan:26257","--insecure")
            try{
                shellRun("cockroach", listOf("userfile","delete",destination).plus(extraParams))
            }
            catch (e:Exception){

            }
            shellRun("cockroach", listOf("userfile","upload",inputFile,destination).plus(extraParams))
        }

    }
}