package ovh.snet.starchaserslauncher.auth

import java.util.*

class Request(
    val username: String,
    val password: String,
    val clientToken: String = UUID.randomUUID().toString(),
    val requestUser: Boolean = false
) {
    val agent = Agent()
}


class Agent {
    val name = "minecraft"
    val version = 1
}