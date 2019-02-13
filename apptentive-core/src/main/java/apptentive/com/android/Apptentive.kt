package apptentive.com.android

import android.app.Application
import apptentive.com.android.core.DependencyProvider

object Apptentive {
    fun register(application: Application) {
        DependencyProvider.register(application)
    }
}