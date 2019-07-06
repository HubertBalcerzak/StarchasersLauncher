package ovh.snet.starchaserslauncher.instance.dto

class MinecraftVersionList(
    val latest: LatestMinecraftVersion,
    val versions: List<MinecraftVersion>
)

class MinecraftVersion(
    val id: String,
    val type: Type,
    val url: String,
    val time: String,
    val releaseTime: String
)

class LatestMinecraftVersion(
    val release: String,
    val snapshot: String
)


enum class Type{
    snapshot,
    release
}