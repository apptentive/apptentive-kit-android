package apptentive.com.android.app

import android.app.Application
import apptentive.com.android.Apptentive

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Apptentive.register(this)
    }
}