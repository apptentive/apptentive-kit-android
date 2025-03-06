# Apptentive Android SDK Kit

The Apptentive Android SDK is the best way to engage your mobile customers.

Use Apptentive features to improve your app's App Store ratings, collect and respond to customer feedback, show surveys at specific points within your app, and more.

#### Get started

In your `build.gradle`, add the following dependency to integrate Apptentive SDK, replacing `“$apptentive_version”` with most recent SDK:
```Groovy
dependencies {
    implementation "com.apptentive:apptentive-kit-android:APPTENTIVE_VERSION"
}
```
Register the SDK in your Application class

```Kotlin
class MyApplication : Application() {
   override fun onCreate() {
       super.onCreate()

       val configuration = ApptentiveConfiguration(
           apptentiveKey = "<YOUR_APPTENTIVE_KEY>",
           apptentiveSignature = "<YOUR_APPTENTIVE_SIGNATURE>"
       ).apply {
           /**
            * Optional parameters:
            * shouldInheritAppTheme           - Default is true
            * logLevel                        - Default is LogLevel.Info
            * shouldSanitizeLogMessages       - Default is true
            * ratingInteractionThrottleLength - Default is TimeUnit.DAYS.toMillis(7)
            * customAppStoreURL               - Default is null (Rating Interaction attempts to show Google In-App Review)
            */
       }
       Apptentive.register(this, configuration)
   }
}
```

Register `ApptentiveActivityInfoCallBack` in your Activity (Can be done in your base Activity class or in every Activities)

```Kotlin
class MainActivity : AppCompatActivity(), ApptentiveActivityInfo {
    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }
}
```
At various points in your app, use the `Apptentive.engage("my_event")` method to record events with Apptentive Android kit. When an event is engaged, the SDK can be configured to display an interaction, such as a Note, Survey, or Love Dialog, and you can define segments based on which events were engaged on your customer's device.

```Kotlin
// Engaging
Apptentive.engage("my_event")

// Engaging with callback (optional)
Apptentive.engage("my_event") { result ->
    when (result) {
        is EngagementResult.InteractionShown -> { /* Interaction was shown */ }
        is EngagementResult.InteractionNotShown -> { /* Interaction was NOT shown */ }
        is EngagementResult.Error -> { /* There was an error during evaluation */ }
        is EngagementResult.Exception -> { /* Something went wrong */ }
    }
}
```
#### Migrating from legacy SDK 

[Android SDK Migration Guide](https://help.alchemer.com/help/android-sdk-5-x-x-to-6-0-0-migration-guide)

#### Binary releases are hosted for Maven 

[Apptentive Maven Central](https://search.maven.org/artifact/com.apptentive/apptentive-kit-android)

#### Reporting Bugs

We encourage you to help us find and fix bugs. If you find a bug, please fill in the contributor agreement, then open a [github issue](https://github.com/apptentive/apptentive-kit-android/issues?direction=desc&sort=created&state=open).
If it is an urgent bug, please contact support@apptentive.com.

#### Contributing

We appreciate contributions to make this SDK better. If you have an improvement or bug fix, please read [CONTRIBUTING.md](CONTRIBUTING.md).

#### Notes

* Make sure you have latest version of our SDK. We're always adding new features!
* Make sure to follow the repo to get updates about features and bug fixes.
