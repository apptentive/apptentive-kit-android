package apptentive.com.app

import android.app.Application
import android.content.Context
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.feedback.RegisterResult
import apptentive.com.android.feedback.SYSTEM
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel
import java.util.concurrent.TimeUnit

val configuration = ApptentiveConfiguration(
    BuildConfig.APPTENTIVE_KEY,
    BuildConfig.APPTENTIVE_SIGNATURE
).apply {
    shouldInheritAppTheme = false
    logLevel = LogLevel.Verbose
    customAppStoreURL = "https://play.google.com/store/apps/details?id=com.apptentive.dogfacts"
    ratingInteractionThrottleLength = TimeUnit.SECONDS.toMillis(30)
}

class MyApplication : Application() {

    override fun onCreate() {
        val prefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        // Turning off by default to get un-redacted logs
        configuration.shouldSanitizeLogMessages = prefs.getBoolean(SHOULD_SANITIZE, false)

        super.onCreate()
        Apptentive.register(this, configuration) {
            when (it) {
                RegisterResult.Success -> Log.v(SYSTEM, "Registration successful")
                is RegisterResult.Failure -> Log.d(
                    SYSTEM,
                    "Registration failed with response code: ${it.responseCode} and error message: ${it.message}"
                )
                is RegisterResult.Exception -> Log.e(
                    SYSTEM,
                    "Registration failed with exception: ${it.error}"
                )
            }
        }
    }
}
