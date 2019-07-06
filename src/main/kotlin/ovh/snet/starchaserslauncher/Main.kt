package ovh.snet.starchaserslauncher

import ovh.snet.starchaserslauncher.instance.InstanceManager
import java.lang.RuntimeException

fun main() {
    val username = ""
    val password = ""
//    val conf = ConfigrationManager()
//    val auth = AuthManager(conf)

    val instanceManager = InstanceManager()

//    val instance = instaceManager.createInstance(instaceManager.getLatestRelease(), "testInstance", null)
    val instance = instanceManager.getInstance("testInstance") ?: throw RuntimeException()

    instanceManager.updateInstance(instance)

//    auth.signIn(username, password)
}