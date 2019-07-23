package ovh.snet.starchaserslauncher.downloader

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.async.Callback
import com.mashape.unirest.http.exceptions.UnirestException
import java.io.File
import java.io.InputStream
import java.lang.RuntimeException
import java.util.*

class FileDownloader {

    private var totalSize: Long = 0
    private var downloadedSize: Long = 0
    var totalFiles: Int = 0
        private set
    private var totalFilesUnknownSize: Int = 0
    private var downloadedFiles: Int = 0
    private var downloadedFilesUnknownSize = 0

    private val fileQueue = ArrayDeque<Pair<DownloadableEntry, Int>>()
    private val failedDownloads = mutableListOf<DownloadableEntry>()
    private val currentlyDownloading = mutableListOf<DownloadableEntry>()


    private val maxRetry = 3

    fun downloadFile(downloadableEntry: DownloadableEntry): FileDownloader {
        addToQueue(downloadableEntry, 0)
        return this
    }

    private fun addToQueue(downloadableEntry: DownloadableEntry, retry: Int) {
        if (retry == 0) {
            totalSize += downloadableEntry.size
            totalFiles++
            if (downloadableEntry.size == 0L) totalFilesUnknownSize++
        }

        if (retry > maxRetry)
            failedDownloads.add(downloadableEntry)
        else
            fileQueue.add(Pair(downloadableEntry, retry))

//        if(currentlyDownloading.size <= 3) fileQueue.poll().let { startDownload(it.first, it.second) }
    }

    private fun startDownload(entry: DownloadableEntry, retry: Int) {
        currentlyDownloading += entry

        if (entry.size < 0) throw RuntimeException("File size less than 0.")
//        if (retry > maxRetry) return

        Unirest.get(entry.downloadLink)
            .asBinaryAsync(object : Callback<InputStream> {
                override fun cancelled() {
                    println("download cancelled")
                    //TODO ???
                }

                override fun completed(response: HttpResponse<InputStream>?) {
                    File(entry.path.replaceAfterLast("\\", "")).mkdirs()
                    File(entry.path).outputStream().use { os -> response?.body?.copyTo(os) }
                    downloadedSize += entry.size
                    downloadedFiles++
                    if (entry.size == 0L) downloadedFilesUnknownSize++
                    currentlyDownloading -= entry

                    if (fileQueue.size >= 0) fileQueue.poll().let { startDownload(it.first, it.second) }
                }

                override fun failed(e: UnirestException?) {
                    println("download ${entry.downloadLink} failed try $retry")
                    currentlyDownloading -= entry
                    addToQueue(entry, retry + 1)
                    if (fileQueue.size >= 0) fileQueue.poll().let { startDownload(it.first, it.second) }
                }
            })
    }

    fun start() {
        if (fileQueue.isNotEmpty())
            fileQueue.poll().let { startDownload(it.first, it.second) }
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

    fun hasError(): Boolean = failedDownloads.size > 0

    fun getErrors(): List<DownloadableEntry> = failedDownloads.toList()
}

public fun download(downloads: List<DownloadableEntry>): FileDownloader =
    downloads.fold(FileDownloader()) { acc, it -> acc.downloadFile(it) }