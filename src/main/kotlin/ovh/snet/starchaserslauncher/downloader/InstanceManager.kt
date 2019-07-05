package ovh.snet.starchaserslauncher.downloader

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.downloader.dto.*
import java.io.File
import java.nio.file.Path

const val INSTANCE_STORAGE_DIR = "instances/"

class InstanceManager {
    //TODO force update
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

        val instanceRoot = Path.of(INSTANCE_STORAGE_DIR, name)
        instanceRoot.toFile().mkdir()

        val (versionManifest, assets) = getManifests(version)

        val downloader = FileDownloader()

        //TODO check file existence
        downloadLibs(versionManifest, instanceRoot.toString(), downloader)
        downloadAssets(assets, instanceRoot.toString(), downloader)
        downloadClient(versionManifest, instanceRoot.toString(), downloader)

        //TODO remove
        while(!downloader.isDone()){
            println(downloader.getProgress())
            Thread.sleep(1000)
        }
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

        val versionManifest = gson.fromJson(
            manifestResponse.body,
            VersionManifest::class.java
        )


        val assetListResponse = Unirest.get(versionManifest.assetIndex.url).asString()

        if (manifestResponse.status != 200) {
            println("Error downloading asset list ${assetListResponse.status}")
            //TODO handle error in gui
        }

        val assetList = gson.fromJson(assetListResponse.body, AssetList::class.java)

        return Pair(versionManifest, assetList)
    }

    private fun downloadLibs(versionManifest: VersionManifest, instanceRoot: String, downloader: FileDownloader) {

        versionManifest.libraries.forEach {
            val path = it.downloads.artifact.path
            val libraryRoot = Path.of(instanceRoot, "libraries").toString()
            //TODO linux support

            downloader.downloadFile(
                "https://libraries.minecraft.net/$path",
                Path.of(libraryRoot, path).toString(),
                it.downloads.artifact.size.toLong()
            )

            if (it.downloads.classifiers?.nativesWindows != null) {
                val nativesPath = it.downloads.classifiers.nativesWindows.path
                downloader.downloadFile(
                    "https://libraries.minecraft.net/$nativesPath",
                    Path.of(libraryRoot, nativesPath).toString(),
                    it.downloads.classifiers.nativesWindows.size.toLong()
                )
            }

        }
    }

    private fun downloadAssets(assetList: AssetList, instanceRoot: String, downloader: FileDownloader) {
        val assetRoot = Path.of(instanceRoot, "assets")
        assetRoot.toFile().mkdirs()

        assetList.objects.entries.forEach { asset ->
            val prefix = asset.value.hash.substring(0, 2)
            Path.of(assetRoot.toString(), prefix).toFile().let { if (!it.exists()) it.mkdirs() }
            downloader.downloadFile(
                "http://resources.download.minecraft.net/$prefix/${asset.value.hash}",
                Path.of(assetRoot.toString(), prefix, asset.value.hash).toString(),
                asset.value.size.toLong()
            )
        }
    }

    private fun downloadClient(versionManifest: VersionManifest, instanceRoot: String, downloader: FileDownloader) {
        downloader.downloadFile(
            versionManifest.downloads.client.url,
            Path.of(instanceRoot, "client.jar").toString(),
            versionManifest.downloads.client.size.toLong()
        )
    }

    private fun initVersionList() {
        if (versionList != null) return

        val response = Unirest.get("https://launchermeta.mojang.com/mc/game/version_manifest.json")
            .asString()

        val gson = Gson()
        versionList = gson.fromJson(response.body, MinecraftVersionList::class.java)

    }

}