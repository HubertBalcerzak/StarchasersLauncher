package ovh.snet.starchaserslauncher.gameLauncher

import java.nio.file.Path

data class MinecraftLauncherData(
    var libraries : List<String>,
    var username : String,
    var version : String,
    var gameDir : Path,
    var assetsDir : Path,
    var assetIndex : String,
    var uuid : String,
    var accessToken : String,
    var userType : String,
    var versionType : String,
    var javaArgs : List<String>,
    var mainClass : String)