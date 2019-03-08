package apptentive.com.android.app

import android.app.Application
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val apptentiveKey = "ANDROID-ANDROID-DEV-c9c0b324114f"
        val apptentiveSignature = "98f5539e9310dc290394c68b76664e98"
        val configuration = ApptentiveConfiguration(apptentiveKey, apptentiveSignature)
        Apptentive.register(this, configuration)
    }
}