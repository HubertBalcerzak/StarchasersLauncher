package ovh.snet.starchaserslauncher.instance

import java.lang.RuntimeException

class InstanceNameTakenException(name: String) : RuntimeException("Instance with name $name already exists")
