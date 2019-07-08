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
    val xmx: String
)

class ModpackFile(
    val path: String,
    val hash: String
)

class IgnoredModpackFile(
    val value: String,
    val type: IgnoreType
)

enum class IgnoreType {
    FILE_EXACT,
    FILE_STARTS_WITH,
    FILE_CONTAINS,
    FOLDER_EXACT,
    FOLDER_STARTS_WITH,
    FOLDER_CONTAINS
}