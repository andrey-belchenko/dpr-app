import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object LoaderCsvFiles  {


    var entitiesFilePath: Path =Paths.get( "C:\\Bin\\entities.csv")
    var linksFilePath: Path =Paths.get( "C:\\Bin\\links.csv")
    var profileFilePath: Path =Paths.get( "C:\\Bin\\profile.csv")
    var entitiesCsvPrinter:CSVPrinter
    var linksCsvPrinter:CSVPrinter
    var profileCsvPrinter:CSVPrinter

    init {
//        val ss = entitiesFilePath?.absolutePathString()
        Files.deleteIfExists(entitiesFilePath)
        Files.deleteIfExists(linksFilePath)
        Files.deleteIfExists(profileFilePath)
        entitiesCsvPrinter = CSVPrinter(Files.newBufferedWriter(entitiesFilePath), CSVFormat.DEFAULT)
        linksCsvPrinter = CSVPrinter(Files.newBufferedWriter(linksFilePath), CSVFormat.DEFAULT)
        profileCsvPrinter = CSVPrinter(Files.newBufferedWriter(profileFilePath), CSVFormat.DEFAULT)
    }

    fun close(){
        entitiesCsvPrinter.flush()
        entitiesCsvPrinter.close()
        linksCsvPrinter.flush()
        linksCsvPrinter.close()
        profileCsvPrinter.flush()
        profileCsvPrinter.close()
    }
}