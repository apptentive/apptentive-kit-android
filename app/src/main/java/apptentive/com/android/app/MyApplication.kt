package apptentive.com.android.app

import android.app.Application
import apptentive.com.android.love.ApptentiveLove

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val apptentiveKey = "apptentive_key"
        val apptentiveSignature = "apptentive_signature"
        ApptentiveLove.register(this, apptentiveKey, apptentiveSignature)
    }
}