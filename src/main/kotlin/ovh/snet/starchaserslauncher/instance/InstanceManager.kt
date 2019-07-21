package ovh.snet.starchaserslauncher.instance

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.downloader.*
import ovh.snet.starchaserslauncher.instance.dto.*
import ovh.snet.starchaserslauncher.modpack.ModpackData
import ovh.snet.starchaserslauncher.modpack.ModpackManifest
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

const val INSTANCE_STORAGE_DIR = "instances/"

class InstanceManager {
    private var versionList: MinecraftVersionList
    private val instanceConfiguration = InstanceConfiguration()
    val gson = Gson()


    init {
        versionList = initVersionList()
        initVersionList()
        File("config").mkdir()
        val instancesDir = File(INSTANCE_STORAGE_DIR)
        if (!instancesDir.exists()) instancesDir.mkdir()
    }

    fun getVersionList(includeSnapshots: Boolean): List<MinecraftVersion> {
        return versionList.versions
    }

    fun getLatestRelease(): MinecraftVersion {
        return versionList.versions.find { it.id == versionList.latest.release && it.type == Type.release }
            ?: throw IllegalStateException("Version list not initialized")
    }

    fun getLatestSnapshot(): MinecraftVersion {
        return versionList.versions.find { it.id == versionList.latest.snapshot }
            ?: throw IllegalStateException("Version list not initialized")
    }


    fun createInstance(version: MinecraftVersion, name: String): Instance {
        if (instanceConfiguration.getInstance(name) != null) {
            //TODO handle error in gui
            println("name taken")
            throw InstanceNameTakenException(name)
        }

        val instance = Instance(name, version.id, "Vanilla", "1G", null)

        instanceConfiguration.addInstance(instance)
        //TODO download modpack manifest

        return instance
    }

    fun createInstance(name: String, modpackLink: String): Instance {
        if (instanceConfiguration.getInstance(name) != null) {
            //TODO handle error in gui
            println("name taken")
            throw InstanceNameTakenException(name)
        }

        val manifest = getModpackManifest(modpackLink)
        val instance = Instance(
            name,
            manifest.data.mcVersion,
            manifest.data.name,
            manifest.data.xmx,
            null
        )
        instanceConfiguration.addInstance(instance)
        return instance
    }

    fun updateInstance(instance: Instance, force: Boolean = false): FileDownloader {
        val instanceRoot = Paths.get(INSTANCE_STORAGE_DIR, instance.name)
        instanceRoot.toFile().mkdir()

        val version = versionList.versions.find { it.id == instance.version }
        val (versionManifest, assets) = getManifests(
            version ?: throw UnknownVersionException(
                instance.version
            )
        )

        val libs = downloadLibs(versionManifest, force)
        val assetsEntry = downloadAssets(assets, true)
        val client = downloadClient(versionManifest, force)
        val root = Entry("root", EntryType.DIRECTORY)
        root.addChildIfNotPresent(Entry(instance.name, EntryType.DIRECTORY))
            .addChildIfNotPresent(
                Entry("instance", EntryType.DIRECTORY)
                    .addChild(libs)
                    .addChild(assetsEntry)
                    .addChild(client)
            )

        val downloader = download(verify(root, instanceRoot.toString()))
        downloader.start()

//        TODO remove
//        println(downloader.totalFiles)
//        while (!downloader.isDone()) {
//            println(downloader.getProgress())
//            Thread.sleep(1000)
//        }
//        println("finished")

        return downloader
    }

    fun getInstanceList(): List<Instance> = instanceConfiguration.getInstanceList()

    fun getInstance(name: String): Instance? = instanceConfiguration.getInstance(name)

    private fun checkName(name: String): Boolean = !Paths.get(INSTANCE_STORAGE_DIR, name).toFile().exists()

    private fun getManifests(version: MinecraftVersion): Pair<VersionManifest, AssetList> {
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

    private fun downloadLibs(
        versionManifest: VersionManifest,
        force: Boolean
    ): Entry {

        val rootEntry = Entry(
            "libraries",
            EntryType.DIRECTORY
        )

        versionManifest.libraries.forEach {
            addLibrary(rootEntry, it.downloads.artifact, force)

            val os = System.getProperty("os.name").toLowerCase()
            if (os.contains("win")
                && it.downloads.classifiers?.nativesWindows != null
            ) addLibrary(rootEntry, it.downloads.classifiers.nativesWindows, force)

            if (os.contains("nix") || os.contains("nux") || os.contains("aix")
                && it.downloads.classifiers?.nativesLinux != null
            ) addLibrary(rootEntry, it.downloads.classifiers?.nativesLinux!!, force)

        }
        return rootEntry
    }

    private fun addLibrary(entry: Entry, artifact: LibraryArtifact, force: Boolean) {
        artifact.path.split("/").fold(entry) { acc, e ->
            acc.addChildIfNotPresent(Entry(e, EntryType.DIRECTORY))
        }.let { finalEntry ->
            finalEntry.initializeFlag = true
            finalEntry.size = artifact.size.toLong()
            finalEntry.type = EntryType.FILE
            finalEntry.downloadLink = artifact.url
            finalEntry.forceDownloadFlag = force
            finalEntry.hash = artifact.sha1
        }
    }

    private fun downloadAssets(assetList: AssetList, force: Boolean): Entry {
        val rootEntry = Entry(
            "assets",
            EntryType.DIRECTORY
        )

        assetList.objects.entries.forEach { asset ->
            val prefix = asset.value.hash.substring(0, 2)
            rootEntry.addChildIfNotPresent(Entry(prefix, EntryType.DIRECTORY))
                .addChildIfNotPresent(
                    Entry(
                        asset.value.hash,
                        EntryType.FILE,
                        initializeFlag = true,
                        downloadLink = "http://resources.download.minecraft.net/$prefix/${asset.value.hash}",
                        hash = asset.value.hash,
                        size = asset.value.size.toLong(),
                        forceDownloadFlag = force
                    )
                )
        }
        return rootEntry
    }

    private fun downloadClient(versionManifest: VersionManifest, force: Boolean): Entry {
        return Entry(
            "client.jar",
            EntryType.FILE,
            initializeFlag = true,
            hash = versionManifest.downloads.client.sha1,
            downloadLink = versionManifest.downloads.client.url,
            size = versionManifest.downloads.client.size.toLong(),
            forceDownloadFlag = force
        )
    }

    private fun initVersionList(): MinecraftVersionList {
        //TODO filter unsupported versions
        val response = Unirest.get("https://launchermeta.mojang.com/mc/game/version_manifest.json")
            .asString()

        val gson = Gson()
        return gson.fromJson(response.body, MinecraftVersionList::class.java)
    }

    private fun getModpackManifest(link: String): ModpackManifest {
        val response = Unirest.get(link)
            .asString()
        if (response.status != 200) {
            //TODO exception
        }
        return gson.fromJson(link, ModpackManifest::class.java)
    }

}