package ovh.snet.starchaserslauncher

import ovh.snet.starchaserslauncher.auth.AuthManager
import ovh.snet.starchaserslauncher.downloader.MinecraftDownloader

fun main() {
    val username = ""
    val password = ""
//    val conf = ConfigrationManager()
//    val auth = AuthManager(conf)

    val minecraftDownloader = MinecraftDownloader()
    minecraftDownloader.createInstance(minecraftDownloader.getLatestRelease(), "testInstance")
//    auth.signIn(username, password)
}