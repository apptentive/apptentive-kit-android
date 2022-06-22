package apptentive.com.android.feedback.lifecycle

internal interface LifecycleListener {
    fun onAppForeground()
    fun onAppBackground()
}
