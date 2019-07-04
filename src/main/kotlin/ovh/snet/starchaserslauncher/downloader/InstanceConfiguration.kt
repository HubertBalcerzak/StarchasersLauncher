package ovh.snet.starchaserslauncher.downloader

import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

const val INSTANCES_CONFIG_LOCATION = "config/instances.conf"

class InstanceConfiguration() {

    private val gson = Gson()
    val instances: MutableList<Instance>


    init {
        val confFile = File(INSTANCES_CONFIG_LOCATION)
        instances = if (confFile.exists()) {
            gson.fromJson(Files.readAllBytes(Path.of(INSTANCES_CONFIG_LOCATION)).toString(), InstanceList::class.java)
                .instances.toMutableList()
        } else ArrayList()
    }

    fun save() {
        Files.write(Path.of(INSTANCES_CONFIG_LOCATION), gson.toJson(InstanceList(instances)).toByteArray())
    }

    fun addInstance(instance: Instance) {
        instances.add(instance)
        save()
    }
}

class InstanceList(
    val instances: List<Instance>
)

class Instance(
    val name: String,
    val version: String,
    val modpackName: String,
    val xmx: String
)
