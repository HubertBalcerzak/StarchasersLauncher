package ovh.snet.starchaserslauncher

import com.google.gson.Gson
import ovh.snet.starchaserslauncher.auth.AuthConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ConfigrationManager {

    private val gson = Gson()

    var authConfiguration: AuthConfiguration? = gson.fromJson(
        Files.readAllBytes(Path.of("config/auth.conf")).toString(),
        AuthConfiguration::class.java
    )

    init {
        val configFolder = File("config/")
        if (!configFolder.exists()) configFolder.mkdir()
    }


    fun saveConfig(config: Any, name: String) = Files.write(
        Path.of("config/$name.conf"), gson.toJson(config).toString().toByteArray()
    )


    fun loadConfig(name: String, clazz: Class<Any>): Any = gson.fromJson(
        Files.readAllBytes(Path.of("config/$name.conf")).toString(), clazz
    )
}