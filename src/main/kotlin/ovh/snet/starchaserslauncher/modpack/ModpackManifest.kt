package ovh.snet.starchaserslauncher.modpack

class ModpackManifest(
    val data: ModpackData,
    val update: List<ModpackFile>,
    val initialize: List<ModpackFile>,
    val ignore: List<IgnoredModpackFile>,
    val forgeLibs: List<ForgeLib>
)


class ModpackData(
    val forge: String,
    val mcVersion: String,
    val name: String,
    val modpackVersion: String,
    val xmx: String,
    val rootEndpoint: String,
    val mainClass: String,
    val minecraftArguments: String
)

class ModpackFile(
    val path: String,
    val hash: String
)

class IgnoredModpackFile(
    val value: String
)

class ForgeLib(
    val path: String,
    val link: String
)

//enum class IgnoreType { //TODO implement
//    EXACT,
//    STARTS_WITH,
//    CONTAINS,
//}