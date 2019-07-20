package ovh.snet.starchaserslauncher.ui.controller

import ovh.snet.starchaserslauncher.instance.Instance
import ovh.snet.starchaserslauncher.instance.InstanceManager
import ovh.snet.starchaserslauncher.instance.dto.MinecraftVersion
import tornadofx.Controller

class InstanceController : Controller() {
    private val instanceManager = InstanceManager()

    fun getMinecraftVersions() : List<DisplayMinecraftVersion> {
      return instanceManager.getVersionList(true).map { DisplayMinecraftVersion(it) }
    }

    fun getInstances() : List<DisplayInstance> {
        return instanceManager.getInstanceList().map { DisplayInstance(it) }
    }

    fun createModpackInstance(name: String, url: String) {
        //TODO change to new method added by Hubertus
        instanceManager.createInstance(getMinecraftVersions().get(0).minecraftVersion, name, url)
    }

    fun createVanillaInstance(name: String, mcVersion: DisplayMinecraftVersion){
        //TODO change to new method added by Hubertus
        instanceManager.createInstance(mcVersion.minecraftVersion, name, "")
    }
}

class DisplayMinecraftVersion(val minecraftVersion: MinecraftVersion){
    override fun toString(): String {
        return minecraftVersion.id
    }
}

class DisplayInstance(val instance: Instance){
    override fun toString(): String {
        return instance.name
    }
}