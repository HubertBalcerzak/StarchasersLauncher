package ovh.snet.starchaserslauncher.ui.controller

import ovh.snet.starchaserslauncher.auth.AuthManager
import ovh.snet.starchaserslauncher.ui.ApplicationContext
import tornadofx.Controller

class LoginController : Controller(){
    private val authManager =  AuthManager()

    fun isSignedIn() = authManager.isSignedIn().also { ApplicationContext.isLoggedIn.set(it)}
    fun logIn(username: String, password: String) = authManager.signIn(username, password).also { isSignedIn() }
    fun logout() {
        authManager.signOut()
        ApplicationContext.isLoggedIn.set(false)
    }

    fun getUserName() : String {
        return authManager.authConfiguration?.username ?: "<Error>"
    }
}