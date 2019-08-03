package ovh.snet.starchaserslauncher.modpack

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import ovh.snet.starchaserslauncher.downloader.FileVerifier
import ovh.snet.starchaserslauncher.exception.ForgeJarException
import java.io.File
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.lang.StringBuilder


class ModpackManifestCreator() {

    private val fileVerifier = FileVerifier()

    fun create(
        forge: String,
        mcVersion: String,
        name: String,
        modpackVersion: String,
        xmx: String,
        rootEndpoint: String,
        rootDir: String
    ): ModpackManifest {
        val forgeManifest = installForge(forge)
        return ModpackManifest(
            ModpackData(
                forge,
                mcVersion,
                name,
                modpackVersion,
                xmx,
                rootEndpoint,
                forgeManifest.mainClass,
                forgeManifest.minecraftArguments
            ),
            processDir(rootDir),
            listOf(),
            listOf(),
            forgeManifest.libraries.map {
                val mavenFile = MavenFile(it.name)
                ForgeLib(
                    mavenFile.getMavenUrl(),
                    (it.url ?: "https://libraries.minecraft.net/") + mavenFile.getMavenUrl()
                )
            }
        )
    }

    private fun processDir(path: String): List<ModpackFile> {
        return processDir(File(path), path)
    }

    private fun processDir(rootFile: File, path: String): List<ModpackFile> {
        val file = File(path)
        return if (file.isFile) listOf(ModpackFile(file.relativeTo(rootFile).path, fileVerifier.getHash(file.path)))
        else file.listFiles()?.fold(mutableListOf()) { acc, it -> acc.addAll(processDir(rootFile, it.path)); acc }
            ?: listOf()
    }

    private fun installForge(forge: String): ForgeManifest {
        val stream =
            Unirest.get("https://files.minecraftforge.net/maven/net/minecraftforge/forge/$forge/forge-$forge-universal.jar")
                .asBinary().body

        val jarFile = Paths.get("tmp", "forge-$forge.jar").toFile()
//        jarFile.outputStream().use { os -> stream?.copyTo(os) }
        val zip = ZipInputStream(stream)
        var a: ZipEntry? = zip.nextEntry
        val gson = Gson()
        while (a != null) {
            if (a.name == "version.json") {
                val string = BufferedReader(InputStreamReader(zip))
                    .lineSequence().asIterable().fold(StringBuilder()) { acc, it -> acc.append(it) }.toString()
                return gson.fromJson(string, ForgeManifest::class.java)
            }
            a = zip.nextEntry

        }

        throw ForgeJarException()
    }
}

class MavenFile(
    val objectName: String
) {
    val group: String
    val name: String
    val version: String

    init {
        objectName.split(":")
            .let {
                group = it[0]
                name = it[1]
                version = it[2]
            }
    }

    fun getMavenUrl(): String {
        if (name == "forge") return "${group.replace(".", "/")}/$name/$version/$name-$version-universal.jar"
        return "${group.replace(".", "/")}/$name/$version/$name-$version.jar"
    }
}