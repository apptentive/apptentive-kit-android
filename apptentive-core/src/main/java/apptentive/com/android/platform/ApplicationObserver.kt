package apptentive.com.android.platform

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class ApplicationObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
    }
}
