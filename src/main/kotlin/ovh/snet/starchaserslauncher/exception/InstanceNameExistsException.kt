package ovh.snet.starchaserslauncher.exception

class InstanceNameExistsException : LauncherException(
    "Instance name taken",
    "Instance with this name already exists"
)