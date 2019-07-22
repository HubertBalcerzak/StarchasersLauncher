package ovh.snet.starchaserslauncher

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
import java.io.InputStream
import java.io.PrintStream




class LauncherApplication : App(MainView::class)

private fun processDir(rootFile: File, path: String): List<ModpackFile> {
    val file = File(path)
    return if (file.isFile) listOf(ModpackFile(file.relativeTo(rootFile).path, ""))
    else file.listFiles()?.fold(mutableListOf()) { acc, it -> acc.addAll(processDir(rootFile, it.path)); acc }
        ?: listOf()
}

fun main() {
//    launch<LauncherApplication>()

    val instance = InstanceManager().getInstance( "asd")!!
//    im.updateInstance(instance!!)

    var mld = MinecraftLauncherData(
        username = "ex-username",
        accessToken = "xx",
        assetIndex = "1.14",
        assetsDir = Paths.get("instances", instance.name, "instance", "assets"),
        gameDir = Paths.get("instances", instance.name, "instance").toAbsolutePath(),
        javaArgs = mutableListOf("-Xmx2G"),
        libraries = processDir(File(Paths.get("instances", instance.name, "instance", "libraries").toAbsolutePath().toString()),
            Paths.get("instances", instance.name, "instance", "libraries").toAbsolutePath().toString()).map{ it.path}.toMutableList()
            .apply { add(Paths.get("instances", instance.name, "instance", "client.jar").toAbsolutePath().toString()) },
        mainClass = "net.minecraft.client.main.Main",
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