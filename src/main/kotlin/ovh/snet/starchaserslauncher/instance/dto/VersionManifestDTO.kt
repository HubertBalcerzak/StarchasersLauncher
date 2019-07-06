package ovh.snet.starchaserslauncher.instance.dto

import com.google.gson.annotations.SerializedName

class VersionManifest(
    val assetIndex: AssetIndex,
    val assets: String,
    val id: String,
    val downloads: Downloads,
    val libraries: List<Library>
)

class AssetIndex(
    val id: String,
    val sha1: String,
    val size: Int,
    val totalSize: Int,
    val url: String
)

class Downloads(
    val client: MainJarDownload,
    val server: MainJarDownload
)

class MainJarDownload(
    val sha1: String,
    val size: Int,
    val url: String
)

class Library(
    val downloads: LibraryDownload,
    val name: String
)

class LibraryDownload(
    val artifact: LibraryArtifact,
    val classifiers: LibraryClassifiers?
)

class LibraryArtifact(
    val path: String,
    val sha1: String,
    val size: String,
    val url: String
)

class LibraryClassifiers(
    @field:SerializedName("natives-linux")
    val nativesLinux: LibraryArtifact?,

    @field:SerializedName("natives-windows")
    val nativesWindows: LibraryArtifact?
)