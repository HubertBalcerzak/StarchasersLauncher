package ovh.snet.starchaserslauncher.instance

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.downloader.Entry
import ovh.snet.starchaserslauncher.downloader.EntryType
import ovh.snet.starchaserslauncher.exception.DownloadErrorException
import ovh.snet.starchaserslauncher.modpack.IgnoredModpackFile
import ovh.snet.starchaserslauncher.modpack.ModpackFile
import ovh.snet.starchaserslauncher.modpack.ModpackManifest
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class ModpackUpdater(
    val instance: Instance
) {

    private val localManifestString: String?
    private val remoteManifestString: String?
    private val gson: Gson = Gson()

    init {
        localManifestString = getLocalString()
        remoteManifestString = getRemoteString()
    }

    fun updateModpack(forceUpdate: Boolean): Entry {
        val manifest = loadModpackManifest()
//        val verify = !checkModpackManifestVersion()

        val rootEntry = Entry(".minecraft", EntryType.DIRECTORY)

        processRegularFiles(manifest.update, rootEntry, manifest.data.rootEndpoint, forceUpdate)
        processInitializeFiles(manifest.initialize, rootEntry, manifest.data.rootEndpoint, forceUpdate)
        if (!forceUpdate) applyIgnored(manifest.ignore, rootEntry)

        return rootEntry
    }

    private fun processRegularFiles(files: List<ModpackFile>, root: Entry, rootEndpoint: String, forceUpdate: Boolean) {
        files.forEach {
            it.path.split("/").fold(root) { acc, pathElement ->
                acc
                    .addChildIfNotPresent(Entry(pathElement, EntryType.DIRECTORY))
            }
                .let { finalEntry ->
                    finalEntry.type = EntryType.FILE
                    finalEntry.size = 0 //TODO store size in modpack manifest?
                    finalEntry.downloadLink = rootEndpoint + it.path
                    finalEntry.forceDownloadFlag = forceUpdate
                    finalEntry.hash = it.hash
                }
        }
    }

    private fun processInitializeFiles(
        files: List<ModpackFile>,
        root: Entry,
        rootEndpoint: String,
        forceUpdate: Boolean
    ) {
        files.forEach {
            it.path.split("/").fold(root) { acc, pathElement ->
                acc
                    .addChildIfNotPresent(Entry(pathElement, EntryType.DIRECTORY))
            }
                .let { finalEntry ->
                    finalEntry.type = EntryType.FILE
                    finalEntry.initializeFlag = true
                    finalEntry.size = 0 //TODO stoe size in modpack manifest?
                    finalEntry.downloadLink = rootEndpoint + it.path
                    finalEntry.forceDownloadFlag = forceUpdate
                    finalEntry.hash = it.hash
                }
        }
    }

    private fun applyIgnored(files: List<IgnoredModpackFile>, root: Entry) {
        files.forEach {
            it.value.split("/").fold(root) { acc, pathElement ->
                acc.addChildIfNotPresent(Entry(pathElement, EntryType.DIRECTORY, ignoreFlag = true))
            }
        }
    }

    private fun checkModpackManifestVersion(): Boolean {
        if (localManifestString.isNullOrBlank() && remoteManifestString.isNullOrBlank())
            throw DownloadErrorException("modpack manifest")
        if (remoteManifestString.isNullOrBlank()) return true
        return remoteManifestString == localManifestString
    }

    private fun loadModpackManifest(): ModpackManifest {
        return if (checkModpackManifestVersion()) gson.fromJson(localManifestString, ModpackManifest::class.java)
        else {
            saveManifest()
            gson.fromJson(remoteManifestString, ModpackManifest::class.java)
        }
    }

    private fun getLocalString(): String? {
        val manifestFile = File("instances/${instance.name}/modpack.json")
        return if (!manifestFile.exists()) null
        else String(Files.readAllBytes(manifestFile.toPath()))
    }

    private fun getRemoteString(): String? {
        if (instance.manifestLink == null || instance.manifestLink.isBlank()) return null
        Unirest.get(instance.manifestLink)
            .asString()
            .let {
                return if (it.status == 200) it.body
                else null
            }
    }

    private fun saveManifest() {
        if (remoteManifestString == null) return
        Files.write(Paths.get("instances", instance.name, "modpack.json"), remoteManifestString.toByteArray())
    }
}