package cc.datafabric.linesapp.scenario.repository

import cc.datafabric.linesapp.sys.repository.common.Repository
import cc.datafabric.linesapp.sys.repository.common.dto.*
import cc.datafabric.linesapp.sys.repository.common.query.Query
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

// for debugging
class TestFileRepository: Repository {
    override fun findEntities(filter: RepositoryEntityFilter): Iterable<RepositoryEntity> {
        TODO("Not yet implemented")
    }

    override fun findLinks(filter: RepositoryLinkFilter): Iterable<RepositoryLink> {
        TODO("Not yet implemented")
    }

    override fun save(diff: RepositoryDiff) {
        val text = diff.toJson()
        val file = File("""C:\Bin\lines-app\diff.json""")
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8"))
        writer.write(text)
        writer.close()
    }

    override  fun executeQueries(queries: Iterable<Query>): RepositoryDataSet {
        val jsonText = File("""C:\Bin\lines-app\dataSet.json""").readText(Charset.forName("UTF-8"))
        return RepositoryDataSet.formJson(jsonText)

    }
}