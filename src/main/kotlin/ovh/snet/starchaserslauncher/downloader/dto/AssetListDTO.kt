package ovh.snet.starchaserslauncher.downloader.dto

class AssetList(
    val objects: Map<String, Asset>
)

class Asset(
    val hash: String,
    val size: Int
)