package apptentive.com.app

import androidx.multidex.MultiDexApplication
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.feedback.RegisterResult
import apptentive.com.android.feedback.SYSTEM
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel

val configuration = ApptentiveConfiguration(
    BuildConfig.APPTENTIVE_KEY,
    BuildConfig.APPTENTIVE_SIGNATURE
).apply {
    // Turning off shouldSanitizeLogMessages, so to get un-redacted logs
    shouldSanitizeLogMessages = false
    logLevel = LogLevel.Verbose
}

class MyApplication : MultiDexApplication() {
    override fun onCreate() {
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
