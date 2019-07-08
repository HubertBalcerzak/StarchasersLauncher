package ovh.snet.starchaserslauncher.downloader

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.async.Callback
import com.mashape.unirest.http.exceptions.UnirestException
import java.io.File
import java.io.InputStream
import java.lang.RuntimeException

class FileDownloader {

    private var totalSize: Long = 0
    private var downloadedSize: Long = 0
    private var totalFiles: Int = 0
    private var totalFilesUnknownSize: Int = 0
    private var downloadedFiles: Int = 0
    private var downloadedFilesUnknownSize = 0

    private val maxRetry = 3

    fun downloadFile(link: String, path: String, size: Long = 0) {
        downloadFile(link, path, size, 0)
    }

    private fun downloadFile(link: String, path: String, size: Long = 0, retry: Int) {
        if (size < 0) throw RuntimeException("File size less than 0.")
        if (retry > maxRetry) return

        if (retry == 0) {
            totalSize += size
            totalFiles++
            if (size == 0L) totalFilesUnknownSize++
        }

        Unirest.get(link)
            .asBinaryAsync(object : Callback<InputStream> {
                override fun cancelled() {
                    println("download cancelled")
                    //TODO some console window?
                }

                override fun completed(response: HttpResponse<InputStream>?) {
                    File(path.replaceAfterLast("\\", "")).mkdirs()
                    File(path).outputStream().use { os -> response?.body?.copyTo(os) }
                    downloadedSize += size
                    downloadedFiles++
                    if (size == 0L) downloadedFilesUnknownSize++
                }

                override fun failed(e: UnirestException?) {
                    println("download $link failed try $retry")
                    downloadFile(link, path, size, retry + 1)
                }
            })
    }

    fun getProgress(): Float {
        if (totalFilesUnknownSize == 0) return downloadedSize / totalSize.toFloat()

        val totalSize = if (totalSize == 0L) totalFiles.toLong() else totalSize

        val averageSize = totalSize / (totalFiles - totalFilesUnknownSize)
        val assumedTotalSize = totalSize + (averageSize * totalFilesUnknownSize)
        val assumedDownloadedSize = downloadedSize + (averageSize * downloadedFilesUnknownSize)

        return assumedDownloadedSize / assumedTotalSize.toFloat()
    }

    fun isDone(): Boolean = totalFiles == downloadedFiles
}