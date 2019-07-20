package ovh.snet.starchaserslauncher.ui.view

import javafx.scene.Parent
import javafx.scene.layout.VBox
import ovh.snet.starchaserslauncher.ui.ApplicationContext
import ovh.snet.starchaserslauncher.ui.controller.LoginController
import ovh.snet.starchaserslauncher.ui.hiddenWhen
import ovh.snet.starchaserslauncher.ui.visibleWhen
import tornadofx.View

class MainView : View() {
    override val root: Parent by fxml("/view/MainView.fxml")
    val loginForm: LoginFormView = find()
    val logged: LoggedView = find()
    val instanceForm: InstanceFormView = find()
    val loginContainer: VBox by fxid("loginContainer")
    val controller: LoginController by inject()

    init {
        logged.visibleWhen { ApplicationContext.isLoggedIn }
        instanceForm.visibleWhen { ApplicationContext.isLoggedIn }
        loginForm.hiddenWhen { ApplicationContext.isLoggedIn }
        loginContainer.apply {
            add(logged)
            add(loginForm)
            add(instanceForm)
        }
        runAsync { controller.isSignedIn() }

    }
}