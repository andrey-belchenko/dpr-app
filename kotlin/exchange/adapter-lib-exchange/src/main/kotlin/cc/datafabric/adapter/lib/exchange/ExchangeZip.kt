package cc.datafabric.adapter.lib.exchange
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object ExchangeZip {

    fun unzipFromStreamToFolder(stream: InputStream, path:String) {
        val zis = ZipInputStream(stream)
        unzipToFolder(zis,path)
    }
  //https://www.baeldung.com/java-compress-and-uncompress
   private  fun unzipToFolder(zipInputStream:ZipInputStream, path:String) {
        val buffer = ByteArray(1024)
        var zipEntry = zipInputStream.nextEntry
        val folder = File(path)
        while (zipEntry != null) {
            val newFile = newFile(folder, zipEntry)
            if (zipEntry.isDirectory) {
                if (!newFile.isDirectory && !newFile.mkdirs()) {
                    throw IOException("Failed to create directory $newFile")
                }
            } else {
                // fix for Windows-created archives
                val parent: File = newFile.parentFile
                if (!parent.isDirectory && !parent.mkdirs()) {
                    throw IOException("Failed to create directory $parent")
                }

                // write file content
                val fos = FileOutputStream(newFile)
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()
    }

   private fun newFile(destinationDir: File, zipEntry: ZipEntry): File {
        val destFile = File(destinationDir, zipEntry.name)
        val destDirPath: String = destinationDir.canonicalPath
        val destFilePath: String = destFile.canonicalPath
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw IOException("Entry is outside of the target dir: " + zipEntry.name)
        }
        return destFile
    }
}