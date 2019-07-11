package ovh.snet.starchaserslauncher.modpack

class CurseManifest(
    val mods: List<CurseMod>
)

class CurseMod(
    val id: String,
    val hash: String
)