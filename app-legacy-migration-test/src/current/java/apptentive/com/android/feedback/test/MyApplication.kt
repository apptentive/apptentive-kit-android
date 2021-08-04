package apptentive.com.android.feedback.test

import androidx.multidex.MultiDexApplication
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.Apptentive._register
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.util.LogLevel

class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        val configuration = ApptentiveConfiguration(
            "ANDROID-ANDROID-DEV-c9c0b324114f",
            "98f5539e9310dc290394c68b76664e98"
        )
        configuration.logLevel = LogLevel.Verbose
        Apptentive.register(this, configuration)
    }
}