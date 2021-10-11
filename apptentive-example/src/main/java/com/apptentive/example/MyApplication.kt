package com.apptentive.example

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * TODO Step 3: Uncomment the code below and import.
         * TODO Step 4: Set your Apptentive Key and Signature
         *  Found on your Apptentive Dashboard at Settings -> API & Development -> SDK Tokens
         */
//        val configuration = ApptentiveConfiguration(
//            apptentiveKey = "YOUR_APPTENTIVE_KEY",
//            apptentiveSignature = "YOUR_APPTENTIVE_SIGNATURE"
//        ).apply {
//            /**
//             * Optional parameters:
//             * logLevel                         - Default is LogLevel.Info
//             * shouldSanitizeLogMessages        - Default is true
//             * ratingInteractionThrottleLength  - Default is TimeUnit.DAYS.toMillis(7)
//             */
//        }
//
//        Apptentive.register(this, configuration)
    }
}