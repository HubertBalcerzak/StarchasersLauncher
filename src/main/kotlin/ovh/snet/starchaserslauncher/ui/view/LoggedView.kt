package ovh.snet.starchaserslauncher.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Label
import ovh.snet.starchaserslauncher.ui.controller.LoginController
import tornadofx.Fragment
import tornadofx.hiddenWhen
import tornadofx.visibleWhen

class LoggedView : Fragment(){
    override val root: Parent by fxml("/view/fragment/LoggedView.fxml")
    val userName : Label by fxid("username")
    val controller: LoginController by inject()

    init {
        runAsync { controller.getUserName() }.ui { userName.text = it }
    }

    @FXML
    fun logout(){
        controller.logout()
    }

}
