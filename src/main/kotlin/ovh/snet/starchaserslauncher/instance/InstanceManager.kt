package ovh.snet.starchaserslauncher.instance

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.downloader.*
import ovh.snet.starchaserslauncher.exception.DownloadErrorException
import ovh.snet.starchaserslauncher.exception.InstanceNameExistsException
import ovh.snet.starchaserslauncher.exception.UnknownVersionException
import ovh.snet.starchaserslauncher.instance.dto.*
import ovh.snet.starchaserslauncher.modpack.ModpackManifest
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile

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


    /**
     * @throws InstanceNameExistsException
     */
    fun createInstance(version: MinecraftVersion, name: String): Instance {
        if (instanceConfiguration.getInstance(name) != null) {
            throw InstanceNameExistsException()
        }

        val instance = Instance(name, version.id, "Vanilla", "1G")

        instanceConfiguration.addInstance(instance)
        return instance
    }

    /**
     * @throws InstanceNameExistsException
     */
    fun createModdedInstance(name: String, modpackLink: String): Instance {
        if (instanceConfiguration.getInstance(name) != null) {
            throw InstanceNameExistsException()
        }

        val manifest = getModpackManifest(modpackLink)
        val instance = Instance(
            name,
            manifest.data.mcVersion,
            manifest.data.name,
            manifest.data.xmx,
            modpackLink,
            false
        )
        instanceConfiguration.addInstance(instance)
        return instance
    }

    fun createModdedInstanceOffline(name: String, modpackManifestPath: String): Instance {
        if (instanceConfiguration.getInstance(name) != null) {
            throw InstanceNameExistsException()
        }

        val manifest = loadModpackManifest(modpackManifestPath)
        val instance = Instance(
            name,
            manifest.data.mcVersion,
            manifest.data.name,
            manifest.data.xmx,
            "",
            false
        )
        instanceConfiguration.addInstance(instance)
        Paths.get("instances", instance.name).toFile().mkdirs()
        Files.write(
            Paths.get("instances", instance.name, "modpack.json"),
            gson.toJson(manifest).toByteArray()
        )
        return instance
    }

    /**
     * @throws UnknownVersionException
     */
    fun updateInstance(instance: Instance, force: Boolean = false): FileDownloader {
        val instanceRoot = Paths.get(INSTANCE_STORAGE_DIR, instance.name, "instance")
        instanceRoot.toFile().mkdir()

        val version = versionList.versions.find { it.id == instance.version }
        val (versionManifest, assets) = getManifests(version ?: throw UnknownVersionException(instance.version))

        val (libs, natives) = downloadLibs(versionManifest, force)
        val assetsEntry = downloadAssets(assets, force)
        val client = downloadClient(versionManifest, force)
        val root = Entry("root", EntryType.DIRECTORY)

        root//.addChildIfNotPresent(Entry(instance.name, EntryType.DIRECTORY))
            .addChildIfNotPresent(
                Entry("instance", EntryType.DIRECTORY)
                    .addChild(libs)
                    .addChild(natives)
                    .addChild(assetsEntry)
                    .addChild(client)
                    .apply {
                        if (!instance.isVanilla) addChild(ModpackUpdater(instance).updateModpack(libs, force))
                    }
            )
        val downloader = download(verify(root, instanceRoot.toString()))
        downloader.start()


//        TODO remove
///////////////////////
        println(downloader.totalFiles)
        while (!downloader.isDone()) {
            println(downloader.getProgress())
            Thread.sleep(1000)
        }
        println("finished")
///////////////////////

        return downloader
    }

    fun getInstanceList(): List<Instance> = instanceConfiguration.getInstanceList()

    fun getInstance(name: String): Instance? = instanceConfiguration.getInstance(name)

    /**
     * To be safe call if at least one file was updated
     */
    fun unpackNatives(instance: Instance) {
        val nativesRoot = Paths.get("instances", instance.name, "natives").toFile()
        nativesRoot.deleteRecursively()
        nativesRoot.mkdirs()

        val jarsRoot = Paths.get("instances", instance.name, "instance", "natives-jars")

        jarsRoot.toFile().listFiles()?.forEach {
            ZipFile(it).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    if (entry.name.endsWith(".dll") || entry.name.endsWith(".so")) {
                        zip.getInputStream(entry).use { input ->
                            Paths.get(nativesRoot.path, entry.name).toFile().outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * @throws DownloadErrorException
     */
    private fun getManifests(version: MinecraftVersion): Pair<VersionManifest, AssetList> {
        val manifestResponse = Unirest.get(version.url)
            .asString()
        if (manifestResponse.status != 200) {
//            println("Error downloading manifest ${manifestResponse.status}")
            throw DownloadErrorException("version manifest")
        }

        val versionManifest = gson.fromJson(
            manifestResponse.body,
            VersionManifest::class.java
        )


        val assetListResponse = Unirest.get(versionManifest.assetIndex.url).asString()

        if (manifestResponse.status != 200) {
//            println("Error downloading asset list ${assetListResponse.status}")
            throw DownloadErrorException("asset list")
        }

        val assetList = gson.fromJson(assetListResponse.body, AssetList::class.java)
        assetList.listLink = versionManifest.assetIndex.url
        assetList.name = version.id + ".json"
        return Pair(versionManifest, assetList)
    }

    private fun downloadLibs(
        versionManifest: VersionManifest,
        force: Boolean
    ): Pair<Entry, Entry> {

        val rootEntry = Entry(
            "libraries",
            EntryType.DIRECTORY
        )
        val natives = Entry(
            "natives-jars",
            EntryType.DIRECTORY
        )

        versionManifest.libraries.forEach {
            if (it.downloads.artifact != null) addLibrary(rootEntry, it.downloads.artifact, force)

            val os = System.getProperty("os.name").toLowerCase()
            if (os.contains("win")
                && it.downloads.classifiers?.nativesWindows != null
            ) addNative(natives, it.downloads.classifiers.nativesWindows, force)

            if (os.contains("nix") || os.contains("nux") || os.contains("aix")
                && it.downloads.classifiers?.nativesLinux != null
            ) addNative(natives, it.downloads.classifiers?.nativesLinux!!, force)

        }
        return Pair(rootEntry, natives)
    }

    private fun addLibrary(entry: Entry, artifact: LibraryArtifact, force: Boolean) {
        artifact.path.split("/").fold(entry) { acc, e ->
            acc.addChildIfNotPresent(Entry(e, EntryType.DIRECTORY))
        }.let { finalEntry ->
            finalEntry.initializeFlag = false
            finalEntry.size = artifact.size.toLong()
            finalEntry.type = EntryType.FILE
            finalEntry.downloadLink = artifact.url
            finalEntry.forceDownloadFlag = force
            finalEntry.hash = artifact.sha1
        }
    }

    private fun addNative(entry: Entry, artifact: LibraryArtifact, force: Boolean) {
        val name = artifact.path.substringAfterLast("/")
        entry.addChildIfNotPresent(
            Entry(
                name,
                EntryType.FILE,
                ignoreFlag = false,
                initializeFlag = false,
                forceDownloadFlag = force,
                hash = artifact.sha1,
                downloadLink = artifact.url,
                size = artifact.size.toLong()
            )
        )
    }

    private fun downloadAssets(assetList: AssetList, force: Boolean): Entry {
        val rootEntry = Entry(
            "assets",
            EntryType.DIRECTORY
        )
        assetList.objects
        val objects = rootEntry.addChildIfNotPresent(Entry("objects", EntryType.DIRECTORY));
        val indexes = rootEntry.addChildIfNotPresent(Entry("indexes", EntryType.DIRECTORY));
        indexes.addChild(
            Entry(
                assetList.name,
                EntryType.FILE,
                downloadLink = assetList.listLink,
                initializeFlag = true
            )
        )
        assetList.objects.entries.forEach { asset ->
            val prefix = asset.value.hash.substring(0, 2)
            objects.addChildIfNotPresent(Entry(prefix, EntryType.DIRECTORY))
                .addChildIfNotPresent(
                    Entry(
                        asset.value.hash,
                        EntryType.FILE,
                        initializeFlag = false,
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

    /**
     * @throws DownloadErrorException
     */
    private fun getModpackManifest(link: String): ModpackManifest {
        val response = Unirest.get(link)
            .asString()
        if (response.status != 200) {
            throw DownloadErrorException("modpack manifest")
        }
        return gson.fromJson(response.body, ModpackManifest::class.java)
    }

    private fun loadModpackManifest(path: String): ModpackManifest {
        return gson.fromJson(String(Files.readAllBytes(Paths.get(path))), ModpackManifest::class.java)
    }


}