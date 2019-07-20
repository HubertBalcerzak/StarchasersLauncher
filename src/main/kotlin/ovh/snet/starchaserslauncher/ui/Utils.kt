package ovh.snet.starchaserslauncher.ui

import javafx.beans.value.ObservableValue
import tornadofx.Fragment
import tornadofx.hiddenWhen
import tornadofx.visibleWhen


fun Fragment.hiddenWhen(exp : ()-> ObservableValue<Boolean>){
    this.root.hiddenWhen(exp)
}


fun Fragment.visibleWhen(exp : ()-> ObservableValue<Boolean>){
    this.root.visibleWhen(exp)
}