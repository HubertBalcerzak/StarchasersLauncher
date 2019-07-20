package ovh.snet.starchaserslauncher.ui.view

import javafx.scene.Parent
import javafx.scene.layout.VBox
import tornadofx.View

class MainView : View(){
    override val root: Parent by fxml("/view/MainView.fxml")
    val loginForm: LoginFormView = find()
    val instanceForm: InstanceFormView = find()
    val loginContainer: VBox by fxid("loginContainer")
     init {
         loginContainer.add(loginForm)
         loginContainer.add(instanceForm)
     }
}