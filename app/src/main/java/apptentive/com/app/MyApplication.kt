package apptentive.com.app

import android.app.Application
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.feedback.RegisterResult
import apptentive.com.android.feedback.SYSTEM
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val configuration = ApptentiveConfiguration(
            "ANDROID-ANDROID-DEV-c9c0b324114f",
            "98f5539e9310dc290394c68b76664e98"
        )
        configuration.logLevel = LogLevel.Verbose
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
