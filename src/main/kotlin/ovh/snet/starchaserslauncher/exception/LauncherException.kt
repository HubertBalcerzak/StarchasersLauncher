package ovh.snet.starchaserslauncher.exception

import java.lang.RuntimeException

abstract class LauncherException(
    val title: String,
    val descpiption: String
) : RuntimeException()