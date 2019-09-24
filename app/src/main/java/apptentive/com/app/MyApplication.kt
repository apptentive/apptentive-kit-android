package apptentive.com.app

import android.app.Application
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val configuration = ApptentiveConfiguration(
            apptentiveKey = "ANDROID-ANDROID-DEV-c9c0b324114f",
            apptentiveSignature = "98f5539e9310dc290394c68b76664e98"
        )
        Apptentive.register(this, configuration)
    }
}