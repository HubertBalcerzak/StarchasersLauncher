package ovh.snet.starchaserslauncher.ui.view

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.ChoiceBox
import ovh.snet.starchaserslauncher.ui.controller.InstanceController
import tornadofx.View

class InstanceFormView : View(){
    override val root: Parent by fxml("/view/fragment/InstanceFormView.fxml")
    val instances: ChoiceBox<String> by fxid("instances")
    val controller : InstanceController by inject()

    @FXML
    fun addInstance() {

    }

    @FXML
    fun removeInstance() {

    }
}