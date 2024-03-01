package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.gridfs.GridFSBuckets
import org.bson.Document
import org.bson.types.ObjectId
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap


object ScriptsRepository {
    private val tempScriptsDir = Paths.get(ExchangeSettingsRepository.tempDir, "scripts").toString()

    fun getProjectPath(name:String):String {
        val projectPath = Paths.get(tempScriptsDir, name)
        extractProjectFromDbIfNeed(name,projectPath.toString())
        return  projectPath.toString()
    }

    private val loadedProjectFileIds = mutableMapOf<String,Any>()
    private val locks = ConcurrentHashMap<String, Any>()

    private fun extractProjectFromDbIfNeed(name:String, projectPath:String) {
        val docs = ExchangeDatabase.configDb.getCollection(Names.Collections.pythonProjects)
            .find(Document("projectName", name)).toList()
        val doc = docs.first()
        val fileId = doc[Names.Fields.fileId]
        val lock = locks.computeIfAbsent(name) { Any() }
        synchronized(lock) {
            if (loadedProjectFileIds[name] != fileId) {
                extractProjectFromDb(projectPath, name, fileId)
            }
        }
    }

    private fun extractProjectFromDb(projectPath: String, name: String, fileId: Any?) {
        Logger.traceFun {
            val folder = File(projectPath)
            if (folder.exists()) {
                folder.deleteRecursively()
            }
            Files.createDirectories(Paths.get(projectPath))
            loadedProjectFileIds[name] = fileId!!
            val gridFSBuckets = GridFSBuckets.create(ExchangeDatabase.configDb);
            val stream = gridFSBuckets.openDownloadStream(fileId as ObjectId)
            ExchangeZip.unzipFromStreamToFolder(stream, projectPath)
        }
    }


}