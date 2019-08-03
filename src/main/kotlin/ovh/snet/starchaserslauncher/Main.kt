package ovh.snet.starchaserslauncher

import com.google.gson.Gson
import com.sun.javaws.ui.LaunchErrorDialog
import ovh.snet.starchaserslauncher.auth.AuthManager
import ovh.snet.starchaserslauncher.gameLauncher.Launcher
import ovh.snet.starchaserslauncher.gameLauncher.MinecraftLauncherData
import ovh.snet.starchaserslauncher.instance.InstanceManager
import ovh.snet.starchaserslauncher.modpack.ModpackFile
import ovh.snet.starchaserslauncher.ui.view.MainView
import tornadofx.App
import tornadofx.launch
import java.io.File
import java.lang.RuntimeException
import java.nio.file.Paths
import java.util.*
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import ovh.snet.starchaserslauncher.gameLauncher.IOManager
import ovh.snet.starchaserslauncher.modpack.ModpackManifestCreator
import java.io.InputStream
import java.io.PrintStream
import java.nio.file.Files


class LauncherApplication : App(MainView::class)

private fun processDir(rootFile: File, path: String): List<ModpackFile> {
    val file = File(path)
    return if (file.isFile) listOf(ModpackFile(file.relativeTo(rootFile).path, ""))
    else file.listFiles()?.fold(mutableListOf()) { acc, it -> acc.addAll(processDir(rootFile, it.path)); acc }
        ?: emptyList()
}

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "generate") {
        val creator = ModpackManifestCreator()
        val scanner = Scanner(System.`in`)
        print("forge version:")
        val forge = scanner.nextLine()
        print("minecraft version")
        val mcVersion = scanner.nextLine()
        print("modpack name:")
        val name = scanner.nextLine()
        print("modpack version:")
        val version = scanner.nextLine()
        print("xmx:")
        val xmx = scanner.nextLine()
        print("root endpoint:")
        val rootEndpoint = scanner.nextLine()
        print("directory:")
        val rootDir = scanner.nextLine()
        val manifest = creator.create(
            forge,
            mcVersion,
            name,
            version,
            xmx,
            rootEndpoint,
            rootDir
        )
        Files.write(Paths.get("modpack.json"), Gson().toJson(manifest).toByteArray())
    } else {
        launch<LauncherApplication>()
    }
}

fun main() {


//    launch<LauncherApplication>()
    val instanceManager = InstanceManager()
//    val instance = instanceManager.createInstance(instanceManager.getVersionList(true).findLast { it.id == "1.12.2" }!!, "test2")
//    val instance = instanceManager.createModdedInstanceOffline("modpackOffline", "modpack.json")
    val instance = InstanceManager().getInstance("modpackOffline")!!
    instanceManager.updateInstance(instance!!)
    instanceManager.unpackNatives(instance)
    var mld = MinecraftLauncherData(
        username = "ex-username",
        accessToken = "xx",
        assetIndex = "1.12.2",
        assetsDir = Paths.get("instances", instance.name, "instance", "assets"),
        gameDir = Paths.get("instances", instance.name, "instance").toAbsolutePath(),
        javaArgs = mutableListOf(
            "-Xmx2G",
            "-Djava.library.path=" + Paths.get("instances", instance.name, "natives").toAbsolutePath()
        ),
        libraries = processDir(
            File(Paths.get("instances", instance.name, "instance", "libraries").toAbsolutePath().toString()),
            Paths.get("instances", instance.name, "instance", "libraries").toAbsolutePath().toString()
        ).map { it.path }.toMutableList()
            .apply { add(Paths.get("instances", instance.name, "instance", "client.jar").toAbsolutePath().toString()) },
        mainClass = "net.minecraft.launchwrapper.Launch",
        userType = "ex-userType",
        uuid = UUID.randomUUID().toString(),
        versionType = "release",
        version = instance.version
    )

    Launcher(mld).runGame().apply {
        IOManager.inheritIO(this.inputStream, System.out)
        IOManager.inheritIO(this.errorStream, System.err)
    }.waitFor()
}