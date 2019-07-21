package ovh.snet.starchaserslauncher.exception

class SignInErrorException : LauncherException(
    "Unable to sign in",
    "Incorrect username or password"
)