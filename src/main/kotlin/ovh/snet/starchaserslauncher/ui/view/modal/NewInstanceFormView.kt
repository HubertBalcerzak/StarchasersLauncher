package ovh.snet.starchaserslauncher.ui.view.modal

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.ComboBox
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import ovh.snet.starchaserslauncher.ui.controller.DisplayMinecraftVersion
import ovh.snet.starchaserslauncher.ui.controller.InstanceController
import tornadofx.Fragment
import tornadofx.disableWhen

class NewInstanceFormView : Fragment() {
    override val root: Parent by fxml("/view/fragment/NewInstanceFormView.fxml")
    val instanceName: TextField by fxid("instanceName")
    val instanceTypeVanilla: RadioButton by fxid("instanceTypeVanilla")
    val instanceTypeModpack: RadioButton by fxid("instanceTypeModpack")
    val instanceMCVersion: ComboBox<DisplayMinecraftVersion> by fxid("instanceMCVersion")
    val instanceURL: TextField by fxid("instanceURL")
    val controller: InstanceController by inject()

    init {
        instanceURL.disableWhen { instanceTypeVanilla.selectedProperty() }
        instanceMCVersion.disableWhen { instanceTypeModpack.selectedProperty() }
        //TODO add loading bar
        runAsync { controller.getMinecraftVersions() }.ui { instanceMCVersion.items.addAll(it) }
    }

    @FXML
    fun addInstance() {
        if (instanceTypeModpack.isSelected) {
            controller.createModpackInstance(instanceName.text, instanceURL.text)
        } else if (instanceTypeVanilla.isSelected) {
            controller.createVanillaInstance(instanceName.text, instanceMCVersion.value)
        }
        close()
    }

    @FXML
    fun cancel() {
        close()
    }
}