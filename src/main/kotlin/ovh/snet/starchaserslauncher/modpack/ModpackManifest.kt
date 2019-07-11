package ovh.snet.starchaserslauncher.modpack

class ModpackManifest(
    val data: ModpackData,
    val update: List<ModpackFile>,
    val initialize: List<ModpackFile>,
    val ignore: List<IgnoredModpackFile>
)


class ModpackData(
    val forge: String,
    val mcVersion: String,
    val name: String,
    val modpackVersion: String,
    val xmx: String,
    val rootEndpoint: String
)

class ModpackFile(
    val path: String,
    val hash: String
)

class IgnoredModpackFile(
    val value: String
)

//enum class IgnoreType { //TODO implement
//    EXACT,
//    STARTS_WITH,
//    CONTAINS,
//}