package ovh.snet.starchaserslauncher.auth

class RefreshResponseDTO(
    val accessToken: String,
    val clientToken: String,
    val selectedProfile: ProfileDTO?
)