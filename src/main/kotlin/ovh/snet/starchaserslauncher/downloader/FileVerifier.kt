package ovh.snet.starchaserslauncher.downloader

import java.io.File
import java.nio.file.Path
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest


class FileVerifier {

    private val md = MessageDigest.getInstance("SHA-1")


    fun verifyExists(path: String): Boolean = verifyExists(File(path))
    fun verifyExists(path: Path): Boolean = verifyExists(path.toFile())
    fun verifyExists(file: File): Boolean = file.exists()


    fun verifyHash(path: Path, hash: String): Boolean = md.digest(Files.readAllBytes(path))
        .let { BigInteger(1, it) }
        .let { it.toString(16) }
        .let { "0".repeat(40 - it.length) + it } == hash

    fun verifyHash(path: String, hash: String): Boolean = verifyHash(Paths.get(path), hash)
    fun verifyHash(file: File, hash: String): Boolean = verifyHash(file.toPath(), hash)

    fun getHash(path: String) = md.digest(Files.readAllBytes(Paths.get(path)))
        .let { BigInteger(1, it) }
        .let { it.toString(16) }
        .let { "0".repeat(40 - it.length) + it }
}

