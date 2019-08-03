package ovh.snet.starchaserslauncher.instance.dto

class AssetList(
    var listLink: String,
    var name: String,
    val objects: Map<String, Asset>
)

class Asset(
    val hash: String,
    val size: Int
)