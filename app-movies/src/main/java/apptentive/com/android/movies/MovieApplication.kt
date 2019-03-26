package apptentive.com.android.movies

import android.app.Application
import apptentive.com.android.love.ApptentiveLove

class MovieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApptentiveLove.register(this, "app-key", "app-signature")
    }
}