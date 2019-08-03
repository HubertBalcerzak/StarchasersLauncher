package ovh.snet.starchaserslauncher.gameLauncher

import java.io.File
import java.nio.file.Paths

class Launcher(private val data: MinecraftLauncherData) {
    fun runGame(): Process {
        return ProcessBuilder(buildCommandArray())
            .directory(File(data.gameDir.toString()))
            .start()
    }

    private fun buildCommandArray(): List<String> {
        return mutableListOf<String>().apply {
            add("java")
            addAll(data.javaArgs)
            add("-cp")
            add(data.libraries
                .map { Paths.get(data.gameDir.toString()).resolve("libraries").resolve(it) }
                .joinToString(File.pathSeparator))
            add(data.mainClass)
            add("--username")
            add(data.username)
            add("--version")
            add(data.version)
            add("--gameDir")
            add(data.gameDir.toAbsolutePath().toString())
            add("--assetsDir")
            add(data.assetsDir.toAbsolutePath().toString())
            add("--assetIndex")
            add(data.assetIndex)
            add("--uuid")
            add(data.uuid)
            add("--accessToken")
            add(data.accessToken)
            add("--userType")
            add(data.userType)
            add("--versionType")
            add(data.versionType)
            add("--tweakClass")
            add("net.minecraftforge.fml.common.launcher.FMLTweaker")
        }
    }
}