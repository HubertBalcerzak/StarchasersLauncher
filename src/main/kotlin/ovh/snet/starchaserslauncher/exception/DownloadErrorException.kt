package ovh.snet.starchaserslauncher.exception

class DownloadErrorException(name: String) : LauncherException(
    "Download error",
    "Unable to download $name. Try again later."
)