package ovh.snet.starchaserslauncher

import ovh.snet.starchaserslauncher.downloader.InstanceManager

fun main() {
    val username = ""
    val password = ""
//    val conf = ConfigrationManager()
//    val auth = AuthManager(conf)

    val minecraftDownloader = InstanceManager()
    minecraftDownloader.createInstance(minecraftDownloader.getLatestRelease(), "testInstance")
//    auth.signIn(username, password)
}