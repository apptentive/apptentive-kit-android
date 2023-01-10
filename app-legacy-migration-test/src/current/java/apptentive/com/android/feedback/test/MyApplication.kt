package apptentive.com.android.feedback.test

import android.app.Application
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.util.LogLevel

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = ApptentiveConfiguration(
            "ANDROID-ANDROID-DEV-c9c0b324114f",
            "98f5539e9310dc290394c68b76664e98"
        )
        configuration.logLevel = LogLevel.Verbose
        configuration.shouldEncryptStorage = true
        Apptentive.register(this, configuration)
    }
}