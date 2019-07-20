package ovh.snet.starchaserslauncher.ui.view

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.ComboBox
import ovh.snet.starchaserslauncher.ui.controller.DisplayInstance
import ovh.snet.starchaserslauncher.ui.controller.InstanceController
import ovh.snet.starchaserslauncher.ui.view.modal.NewInstanceFormView
import tornadofx.Fragment
import tornadofx.View

class InstanceFormView : Fragment() {
    override val root: Parent by fxml("/view/fragment/InstanceFormView.fxml")
    val instances: ComboBox<DisplayInstance> by fxid("instances")
    val controller: InstanceController by inject()

    init {
        runAsync { controller.getInstances() }.ui { instances.items.addAll(it) }
    }

    @FXML
    fun addInstance() {
        find<NewInstanceFormView>().openModal(escapeClosesWindow = false, owner = currentWindow, block = true)
    }

    @FXML
    fun removeInstance() {

    }

    @FXML
    fun play() {

    }
}