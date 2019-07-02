package ovh.snet.starchaserslauncher.auth

class AuthenticationResponseDTO(
    val accessToken: String,
    val clientToken: String,
    val availableProfiles: List<ProfileDTO>,
    val selectedProfile: ProfileDTO?
)



class ProfileDTO(
    val id: String,
    val name: String
)