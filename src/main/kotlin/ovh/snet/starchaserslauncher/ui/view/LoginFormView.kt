package ovh.snet.starchaserslauncher.ui.view

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.CheckBox
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import ovh.snet.starchaserslauncher.ui.controller.LoginController
import tornadofx.Fragment

class LoginFormView : Fragment(){
    override val root: Parent by fxml("/view/fragment/LoginForm.fxml")
    private val login : TextField by fxid("login")
    private val password : PasswordField by fxid("password")
    private val remember : CheckBox by fxid("remember")
    private val controller : LoginController by inject()

    @FXML
    fun login(){
        controller.logIn(login.text, password.text)
    }
}