package ovh.snet.starchaserslauncher.downloader

class Entry(
    val name: String,
    var type: EntryType,
    var ignoreFlag: Boolean = false,
    var initializeFlag: Boolean = false,
    var forceDownloadFlag: Boolean = false,
    var hash: String = "",
    var downloadLink: String = "",
    var size: Long = 0

) {
    val children: HashMap<String, Entry> = HashMap()
    private var validated: Boolean = false
    var visited: Boolean = false

    fun validate() {
        validated = true
    }

    operator fun get(name: String): Entry? = children[name]

    fun addChild(entry: Entry): Entry {
        children[entry.name] = entry
        return this
    }

    fun addChildIfNotPresent(entry: Entry): Entry {
        children.putIfAbsent(entry.name, entry)
        return children[entry.name]!!
    }
}


class DownloadableEntry(
    val path: String,
    val downloadLink: String,
    val size: Long
)

enum class EntryType {
    FILE,
    DIRECTORY
}

