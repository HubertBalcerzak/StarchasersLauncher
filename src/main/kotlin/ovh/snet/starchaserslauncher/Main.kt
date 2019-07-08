package ovh.snet.starchaserslauncher

import ovh.snet.starchaserslauncher.auth.AuthManager
import ovh.snet.starchaserslauncher.instance.InstanceManager
import java.lang.RuntimeException

fun main() {
    val username = ""
    val password = ""
    val auth = AuthManager()
//    println(auth.authConfiguration?.accessToken)
//    auth.validate()
//    println(auth.authConfiguration?.accessToken)

    val instanceManager = InstanceManager()

//    val instance = instaceManager.createInstance(instaceManager.getLatestRelease(), "testInstance", null)
//    val instance = instanceManager.getInstance("testInstance") ?: throw RuntimeException()

//    instanceManager.updateInstance(instance)

//    auth.signIn(username, password)
}