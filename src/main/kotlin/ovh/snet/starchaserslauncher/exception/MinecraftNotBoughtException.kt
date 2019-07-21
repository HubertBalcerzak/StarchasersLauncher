package ovh.snet.starchaserslauncher.exception

import java.lang.RuntimeException

class MinecraftNotBoughtException: LauncherException(
    "Minecraft not bought",
    "Minecraft was not found on this Mojang account"
)