package ovh.snet.starchaserslauncher.auth

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.exception.MinecraftNotBoughtException
import ovh.snet.starchaserslauncher.exception.SignInErrorException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private const val MOJANG_AUTH_ROOT_URL = "https://authserver.mojang.com"
private const val AUTH_CONFIG_LOCATION = "config/auth.conf"

class AuthManager {
    var authConfiguration: AuthConfiguration? = null
        private set

    private val gson = Gson()

    init {
        loadConfiguration()
    }


    /**
     * @throws SignInErrorException
     * @throws MinecraftNotBoughtException
     */
    fun signIn(username: String, password: String) {
        val response = Unirest
            .post("$MOJANG_AUTH_ROOT_URL/authenticate")
            .header("Content-Type", "application/json")
            .body(gson.toJson(Request(username, password)))
            .asString()
        if (response.status != 200) {
            val error = gson.fromJson(response.body, ErrorDTO::class.java)
//            println(error.errorMessage)
            throw SignInErrorException()
        } else {
            val auth = gson.fromJson(response.body, AuthenticationResponseDTO::class.java)
            if (auth.selectedProfile == null) {
                throw MinecraftNotBoughtException()
            }

            authConfiguration = AuthConfiguration(
                auth.selectedProfile.name,
                auth.accessToken,
                auth.clientToken
            )
        }
        saveConfiguration()
    }

    private fun refreshToken(): Boolean {
        if (authConfiguration == null) return false
        val response = Unirest.post("$MOJANG_AUTH_ROOT_URL/refresh")
            .body(gson.toJson(ValidateDTO(authConfiguration!!.accessToken, authConfiguration!!.id)))
            .asString()
        if (response.status != 200) return false
        val refreshDTO = gson.fromJson(response.body, RefreshResponseDTO::class.java)
        authConfiguration =
            AuthConfiguration(authConfiguration!!.username, refreshDTO.accessToken, refreshDTO.clientToken)
        saveConfiguration()
        return true
    }

    fun validate(): Boolean {
        if (authConfiguration == null) return false
        val response = Unirest.post("$MOJANG_AUTH_ROOT_URL/validate")
            .body(gson.toJson(ValidateDTO(authConfiguration!!.accessToken, authConfiguration!!.id)))
            .asString()

        return response.status == 204
    }

    fun signOut(): Boolean {
        if (authConfiguration == null) return true
        val response = Unirest.post("$MOJANG_AUTH_ROOT_URL/invalidate")
            .header("Content-Type", "application/json")
            .body(gson.toJson(ValidateDTO(authConfiguration!!.accessToken, authConfiguration!!.id)))
            .asString()

        authConfiguration = null
        Files.delete(Paths.get(AUTH_CONFIG_LOCATION))
        return response.status == 200
    }

    fun isSignedIn() = authConfiguration != null

    private fun saveConfiguration() {
        if (authConfiguration == null) return

        Files.write(Paths.get(AUTH_CONFIG_LOCATION), gson.toJson(authConfiguration).toByteArray())
    }

    private fun loadConfiguration() {
        File("config").mkdir()
        val confFile = File(AUTH_CONFIG_LOCATION)
        if (!confFile.exists()) {
            authConfiguration = null
            return
        }

        authConfiguration = gson.fromJson(String(Files.readAllBytes(confFile.toPath())), AuthConfiguration::class.java)

        if (!validate()) {
            refreshToken()
        }
    }
}