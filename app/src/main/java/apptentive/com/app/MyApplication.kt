package apptentive.com.app

import android.app.Application
import apptentive.com.android.core.DependencyProvider

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        DependencyProvider.register(this)
    }
}