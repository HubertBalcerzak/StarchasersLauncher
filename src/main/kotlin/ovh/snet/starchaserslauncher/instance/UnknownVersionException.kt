package ovh.snet.starchaserslauncher.instance

import java.lang.RuntimeException

class UnknownVersionException(version: String) : RuntimeException("Unknown version $version")