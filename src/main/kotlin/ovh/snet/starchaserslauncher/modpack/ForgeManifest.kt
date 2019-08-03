package ovh.snet.starchaserslauncher.modpack

class ForgeManifest(
    val mainClass: String,
    val minecraftArguments: String,
    val libraries: List<ForgeLibrary>
)


class ForgeLibrary(
    val name: String,
    val url: String?,
    val serverreq: Boolean,
    val clientreq: Boolean
)

