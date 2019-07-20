package ovh.snet.starchaserslauncher.ui.controller

import ovh.snet.starchaserslauncher.auth.AuthManager
import tornadofx.Controller

class LoginController : Controller(){
    private val authManager =  AuthManager()

    fun isSignedIn() = authManager.isSignedIn()
    fun logIn(username: String, password: String) = authManager.signIn(username, password)
}