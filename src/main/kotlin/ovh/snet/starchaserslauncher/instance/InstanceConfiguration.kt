package ovh.snet.starchaserslauncher.instance

import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val INSTANCES_CONFIG_LOCATION = "config/instances.conf"

class InstanceConfiguration() {

    private val gson = Gson()
    val instances: MutableMap<String, Instance>


    init {
        //TODO better error handling(empty config file, corrupt config)
        val confFile = File(INSTANCES_CONFIG_LOCATION)
        instances = if (confFile.exists()) {
            gson.fromJson(String(Files.readAllBytes(Paths.get(INSTANCES_CONFIG_LOCATION))), InstanceList::class.java)
                .instances.fold(
                HashMap(),
                { map: HashMap<String, Instance>, instance: Instance -> map[instance.name] = instance; map })
        } else HashMap()
    }

    fun save() {
        Files.write(
            Paths.get(INSTANCES_CONFIG_LOCATION),
            gson.toJson(InstanceList(instances.entries.map { it.value })).toByteArray()
        )
    }

    fun addInstance(instance: Instance) {
        instances[instance.name] = instance
        save()
    }

    fun getInstance(name: String): Instance? {
        return instances[name]
    }

    fun getInstanceList(): List<Instance> = instances.entries.map { it.value }

    fun deleteInstance(name: String) {
        instances.remove(name)
        save()
    }

    fun deleteInstance(instance: Instance) {
        deleteInstance(instance.name)
    }

}

class InstanceList(
    val instances: List<Instance>
)

class Instance(
    val name: String,
    val version: String,
    val modpackName: String,
    val xmx: String,
    val manifestLink: String = "",
    val isVanilla: Boolean = true
)
