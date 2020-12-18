package apptentive.com.android.ui

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import apptentive.com.android.core.Callback

open class ApptentiveViewModel : ViewModel() {
    internal var clearCallback: Callback? = null

    @CallSuper
    override fun onCleared() {
        clearCallback?.invoke()
        clearCallback = null
    }
}