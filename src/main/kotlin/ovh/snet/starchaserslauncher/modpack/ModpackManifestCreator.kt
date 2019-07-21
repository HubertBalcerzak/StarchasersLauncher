package ovh.snet.starchaserslauncher.modpack

import ovh.snet.starchaserslauncher.downloader.FileVerifier
import java.io.File

class ModpackManifestCreator() {

    private val fileVerifier = FileVerifier()

    fun create(
        forge: String,
        mcVersion: String,
        name: String,
        modpackVetsion: String,
        xmx: String,
        rootEndpoint: String,
        rootDir: String
    ): ModpackManifest = ModpackManifest(
        ModpackData(forge, mcVersion, name, modpackVetsion, xmx, rootEndpoint),
        processDir(rootDir),
        listOf(),
        listOf()
    )

    private fun processDir(path: String): List<ModpackFile> {
        return processDir(File(path), path)
    }

    private fun processDir(rootFile: File, path: String): List<ModpackFile> {
        val file = File(path)
        return if (file.isFile) listOf(ModpackFile(file.relativeTo(rootFile).path, fileVerifier.getHash(file.path)))
        else file.listFiles()?.fold(mutableListOf()) { acc, it -> acc.addAll(processDir(rootFile, it.path)); acc }
            ?: listOf()
    }
}