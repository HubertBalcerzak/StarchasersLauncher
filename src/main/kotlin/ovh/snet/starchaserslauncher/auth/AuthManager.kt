package ovh.snet.starchaserslauncher.auth

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.ConfigrationManager

class AuthManager(
    private val configurationManager: ConfigrationManager
) {


    fun signIn(username: String, password: String) {
        val gson = Gson()
        val response = Unirest
            .post("https://authserver.mojang.com/authenticate")
            .header("Content-Type", "application/json")
            .body(gson.toJson(Request(username, password)))
            .asString()
        if (response.status != 200) {
            val error = gson.fromJson<ErrorDTO>(response.body, ErrorDTO::class.java)
            println(error.errorMessage)
            //TODO handle in gui
        } else {
            val auth = gson.fromJson<AuthenticationResponseDTO>(response.body, AuthenticationResponseDTO::class.java)
            if (auth.selectedProfile == null) {
                //TODO handle error
                return
            }

            configurationManager.authConfiguration = AuthConfiguration(
                auth.selectedProfile.name,
                auth.accessToken,
                auth.clientToken
            )

            println(auth.accessToken)
        }

    }

    private fun refreshToken() {

    }
}