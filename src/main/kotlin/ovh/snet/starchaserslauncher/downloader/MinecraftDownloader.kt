package ovh.snet.starchaserslauncher.downloader

import com.google.gson.Gson
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.async.Callback
import com.mashape.unirest.http.exceptions.UnirestException
import ovh.snet.starchaserslauncher.downloader.dto.*
import java.io.File
import java.io.InputStream
import java.nio.file.Path

const val INSTANCE_STORAGE_DIR = "instances/"

class MinecraftDownloader {
    var versionList: MinecraftVersionList? = null

    init {
        val instancesDir = File(INSTANCE_STORAGE_DIR)
        if (!instancesDir.exists()) instancesDir.mkdir()
    }

    fun getVersionList(includeSnapshots: Boolean): List<MinecraftVersion> {
        initVersionList()
        return versionList?.versions ?: throw IllegalStateException("Version list not initialized")
        //TODO filter unsupported versions
    }

    fun getLatestRelease(): MinecraftVersion {
        initVersionList()
        return versionList?.versions?.find { it.id == versionList?.latest?.release && it.type == Type.release }
            ?: throw IllegalStateException("Version list not initialized")
    }

    fun getLatestSnapshot(): MinecraftVersion {
        initVersionList()
        return versionList?.versions?.find { it.id == versionList?.latest?.snapshot }
            ?: throw IllegalStateException("Version list not initialized")
    }


    fun createInstance(version: MinecraftVersion, name: String) {
        if (!checkName(name)) {
            //TODO handle error in gui
            println("name taken")
            return
        }
        Path.of(INSTANCE_STORAGE_DIR, name).toFile().mkdir()

        val (versionManifest, assets) = getManifests(version)

        downloadLibs(versionManifest, Path.of(INSTANCE_STORAGE_DIR, name).toString())

    }

    private fun checkName(name: String): Boolean = !Path.of(INSTANCE_STORAGE_DIR, name).toFile().exists()

    private fun getManifests(version: MinecraftVersion): Pair<VersionManifest, AssetList> {
        val gson = Gson()
        val manifestResponse = Unirest.get(version.url)
            .asString()
        if (manifestResponse.status != 200) {
            println("Error downloading manifest ${manifestResponse.status}")
            //TODO handle error in gui
        }

        val versionManifest = gson.fromJson<VersionManifest>(
            manifestResponse.body,
            VersionManifest::class.java
        )


        val assetListResponse = Unirest.get(versionManifest.assetIndex.url).asString()

        if (manifestResponse.status != 200) {
            println("Error downloading asset list ${assetListResponse.status}")
            //TODO handle error in gui
        }

        val assetList = gson.fromJson<AssetList>(assetListResponse.body, AssetList::class.java)

        return Pair(versionManifest, assetList)
    }

    private fun downloadLibs(versionManifest: VersionManifest, instanceRoot: String) {
        versionManifest.libraries.forEach {
            val path = it.downloads.artifact.path
            val downloadPath = it.downloads.classifiers?.nativesWindows?.path ?: path
            val libraryRoot = Path.of(instanceRoot, "libraries").toString()
            //TODO linux support

            Unirest.get("https://libraries.minecraft.net/$downloadPath")
                .asBinaryAsync(object : Callback<InputStream> {
                    override fun cancelled() {
                        println("download cancelled")
                    }

                    override fun completed(response: HttpResponse<InputStream>?) {
                        Path.of(libraryRoot, path.replaceAfterLast("/", "")).toFile().mkdirs()
                        Path.of(libraryRoot, path).toFile().outputStream().use { os -> response?.body?.copyTo(os) }
                    }

                    override fun failed(e: UnirestException?) {
                        println("download $downloadPath failed")
                    }
                })
            println("started download ${it.name}")
        }
    }

    private fun initVersionList() {
        if (versionList != null) return

        val response = Unirest.get("https://launchermeta.mojang.com/mc/game/version_manifest.json")
            .asString()

        val gson = Gson()
        versionList = gson.fromJson(response.body, MinecraftVersionList::class.java)

    }

}