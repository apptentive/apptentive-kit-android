package apptentive.com.android

import android.app.Application
import apptentive.com.android.core.Provider

object Apptentive {
    fun register(application: Application) {
        Provider.register(application)
    }
}