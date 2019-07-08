package ovh.snet.starchaserslauncher.instance

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.modpack.ModpackManifest
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


class ModpackUpdater(
    val instance: Instance
) {

    private val localManifestString: String?
    private val remoteManifestString: String?
    private val gson: Gson = Gson()

    init {
        localManifestString = getLocalString()
        remoteManifestString = getRemoteString()
    }

    fun updateModpack(forceVerify: Boolean, forceUpdate: Boolean) {
        val manifest = loadModpackManifest()
        val verify = forceVerify || !checkModpackManifestVersion()
        if(!verify && !forceUpdate) return


    }

    private fun checkModpackManifestVersion(): Boolean = localManifestString == remoteManifestString

    private fun loadModpackManifest(): ModpackManifest {
        return if (checkModpackManifestVersion()) gson.fromJson(localManifestString, ModpackManifest::class.java)
        else {
            saveManifest()
            gson.fromJson(remoteManifestString, ModpackManifest::class.java)
        }
    }

    private fun getLocalString(): String? {
        val manifestFile = File("instances/${instance.name}/modpack.json")
        return if (!manifestFile.exists()) null
        else String(Files.readAllBytes(manifestFile.toPath()))
    }

    private fun getRemoteString(): String? {
        if (instance.manifestLink == null || instance.manifestLink.isBlank()) return null
        Unirest.get(instance.manifestLink)
            .asString()
            .let {
                return if (it.status == 200) it.body
                else null
                //TODO hanlde error in gui
            }
    }

    private fun saveManifest() {
        if (remoteManifestString == null) return
        Files.write(Path.of("instances", instance.name, "modpack.json"), remoteManifestString.toByteArray())
    }
}