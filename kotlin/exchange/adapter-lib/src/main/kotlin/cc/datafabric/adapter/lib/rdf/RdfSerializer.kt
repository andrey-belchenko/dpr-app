package cc.datafabric.adapter.lib.rdf



import org.apache.jena.query.ARQ
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFFormat
import java.io.ByteArrayOutputStream

object RdfSerializer {
    init {
        //была ошибка в собранном jar, которая не воспроизводилась при дебаге
        //https://stackoverflow.com/questions/54905185/how-to-debug-nullpointerexception-at-apache-jena-queryexecutionfactory-during-cr
        ARQ.init()
    }


    fun modelToJson(model: RdfModel):String {
        ByteArrayOutputStream().use {
            RDFDataMgr.write(it, model.coreObject, RDFFormat.JSONLD_PRETTY)
            return it.toString()
        }
    }

    fun modelToJsonFlat(model: RdfModel):String {
        ByteArrayOutputStream().use {
            RDFDataMgr.write(it, model.coreObject, RDFFormat.JSONLD_FLAT)
            return it.toString()
        }
    }

    fun modelToQuads(model: RdfModel):String {
        ByteArrayOutputStream().use {
            RDFDataMgr.write(it, model.coreObject, RDFFormat.NQUADS)
            return it.toString()
        }
    }

    fun modelToPlainXml(model: RdfModel):String {
        ByteArrayOutputStream().use {
            RDFDataMgr.write(it,model.coreObject, RDFFormat.RDFXML_PLAIN)
            return it.toString()
        }
    }

    fun modelToXml(model: RdfModel):String {
        ByteArrayOutputStream().use {
            RDFDataMgr.write(it, model.coreObject, RDFFormat.RDFXML)
            return it.toString()
        }
    }










}