package ovh.snet.starchaserslauncher

import ovh.snet.starchaserslauncher.auth.AuthManager

fun main() {
    val username = ""
    val password = ""
    val conf = ConfigrationManager()
    val auth = AuthManager(conf)
//    auth.signIn(username, password)
}