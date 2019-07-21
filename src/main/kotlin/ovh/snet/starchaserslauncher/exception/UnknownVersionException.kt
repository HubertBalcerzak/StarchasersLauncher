package ovh.snet.starchaserslauncher.exception


class UnknownVersionException(version: String) : LauncherException(
    "Unknown version",
    "Unknown version $version"
)