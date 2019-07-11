package ovh.snet.starchaserslauncher.downloader

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

fun verify(entry: Entry, root: String): List<DownloadableEntry> {
    return traverseDir(File(root), entry, FileVerifier())
}

//YIKES
private fun traverseDir(
    currentFile: File,
    entry: Entry,
    fileVerifier: FileVerifier
): List<DownloadableEntry> {
    val currentEntry = entry[currentFile.name]
    if (currentEntry?.ignoreFlag == true) return listOf()

    if (currentFile.isFile) {
        return if (currentEntry == null) {
            currentFile.delete()
            listOf()
        } else if (currentEntry.type == EntryType.DIRECTORY) {
            downloadAll(currentFile.path, currentEntry)
        } else {
            currentEntry.visited = true
            if ((currentEntry.initializeFlag || fileVerifier.verifyHash(
                    currentFile,
                    currentEntry.hash
                )) && !currentEntry.forceDownloadFlag
            ) {
                listOf()
            } else {
                currentFile.delete()
                listOf(DownloadableEntry(currentFile.path, currentEntry.downloadLink, currentEntry.size))
            }
        }
    } else if (currentFile.isDirectory) {
        return if (currentEntry == null || currentEntry.type == EntryType.FILE) {
            currentFile.deleteRecursively()
            listOf()
        } else {
            currentEntry.visited = true
            return currentFile.listFiles()!!.map { traverseDir(it, currentEntry, fileVerifier) }
                .fold(ArrayList<DownloadableEntry>()) { acc, it -> acc.addAll(it); acc }
                .let { it.addAll(downloadAll(currentFile.path, currentEntry)); it }
        }
    }

    return entry.children.entries.filter { !it.value.visited }
        .map { downloadAll(Paths.get(currentFile.path, it.value.name).toString(), it.value) }
        .fold(ArrayList()) { acc, it -> acc.addAll(it); acc }


}

private fun downloadAll(currentPath: String, entry: Entry): List<DownloadableEntry> =
    entry.children.entries.fold(ArrayList()) { acc, it ->
        if (!(it.value.ignoreFlag || it.value.visited)) {
            if (it.value.type == EntryType.DIRECTORY) acc.addAll(
                downloadAll(
                    Paths.get(currentPath, it.value.name).toString(),
                    it.value
                )
            )
            else acc.add(
                DownloadableEntry(
                    Paths.get(currentPath, it.value.name).toString(),
                    it.value.downloadLink,
                    it.value.size
                )
            )
        }
        acc
    }