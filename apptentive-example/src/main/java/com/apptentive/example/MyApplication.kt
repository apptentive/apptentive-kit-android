package com.apptentive.example

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * TODO Step 3: Uncomment the code below and import.
         * TODO Step 4: Set your Apptentive Key and Signature
         *  Found on your Apptentive Dashboard at Settings -> API & Development -> SDK Tokens
         *
         *  If you'd like to demo the Apptentive Rating Dialog in this example app, set the
         *  customAppStoreURL to a URL. Preferably to an app store URL, though any URL will work.
         */
//        val configuration = ApptentiveConfiguration(
//            apptentiveKey = "YOUR_APPTENTIVE_KEY",
//            apptentiveSignature = "YOUR_APPTENTIVE_SIGNATURE"
//        ).apply {
//            /**
//             * Optional parameters:
//             * logLevel                        - Default is LogLevel.Info
//             * shouldSanitizeLogMessages       - Default is true
//             * ratingInteractionThrottleLength - Default is TimeUnit.DAYS.toMillis(7)
//             * customAppStoreURL               - Default is null (Rating Interaction attempts to show Google In-App Review)
//             */
//        }
//
//        Apptentive.register(this, configuration)
    }
}