package apptentive.com.android.feedback

import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.LogLevel
import java.util.concurrent.TimeUnit

/**
 * This class creates a new ApptentiveConfiguration object which is used to initialize the
 * Apptentive SDK.
 * @param apptentiveKey The Apptentive Key that should be used when connecting to
 * the Apptentive API.
 * @param apptentiveSignature The Apptentive Signature that should be used when connecting to
 * the Apptentive API.
 */
data class ApptentiveConfiguration(
    val apptentiveKey: String,
    val apptentiveSignature: String
) {
    /**
     * Determines if Apptentive Interactions will use the host app's theme or Apptentive's theme.
     *
     * If `true`, Apptentive Interactions will use the host app's theme and colors.
     *
     * If `false`, Apptentive Interactions will use the `Theme.Apptentive` theme and colors,
     * set in the `styles.xml` file within the `apptentive-core-ui` package.
     *
     * `ApptentiveThemeOverride` will always take priority over all themes.
     * See `ThemeHelper.kt` within the `apptentive-core-ui` package for more info.
     */
    var shouldInheritAppTheme: Boolean = true

    /**
     * [LogLevel] is used to define what level of logs we will show in Logcat
     *
     * [LogLevel.Verbose] - Any relevant info not shown in other log Levels
     *
     * [LogLevel.Debug]   - Processes with more technical information
     *
     * [LogLevel.Info]    - General processes and non-technical results
     *
     * [LogLevel.Warning] - Non-breaking / handled issues
     *
     * [LogLevel.Error]   - Breaking / unhandled issues ([Throwable])
     */
    var logLevel: LogLevel = LogLevel.Info

    /**
     * Redacts sensitive information from being logged when set to `true`.
     *
     * Data fields which have `@SensitiveDataKey` annotation will replace the logged data with
     * [Constants.REDACTED_DATA]
     */
    var shouldSanitizeLogMessages: Boolean = true

    /**
     * Determines if the on-device storage should be encrypted.
     *
     * If `true` on-device storage of Apptentive SDK is encrypted
     *
     * If `false` on-device storage of Apptentive SDK is not encrypted
     *
     * The app can use this option if it is sending any sensitive information
     * to Apptentive SDK through custom data
     *
     * Check out the documentation [here](https://learn.apptentive.com/knowledge-base/android-integration-guide/#device-storage-encryption)
     */
    var shouldEncryptStorage: Boolean = false

    /**
     * A time based throttle which determines when a rating interaction can be shown again.
     * This is a safeguard on top of the criteria already set in the Apptentive Dashboard.
     * This applies to both Google In-App Review & Apptentive Rating Dialog interactions.
     *
     * Use [TimeUnit] for simple conversion utils
     *
     * Example: `TimeUnit.DAYS.toMillis(30)`
     */
    var ratingInteractionThrottleLength = TimeUnit.DAYS.toMillis(7)

    /**
     * The URL that the user will be directed to using the Apptentive Rating Dialog.
     *
     * The main purpose of this configuration is to support alternate app stores.
     * By supplying your custom app store URL here, the user will be directed to
     * the provided URL when prompted to rate your app.
     *
     * **DO NOT** use if you wish to use Google In-App Review.
     *
     * When `null` (this is the default), the user will see the Google In-App Review and
     * will be able to leave an app rating to the Play Store listing of the app.
     *
     * When set to a `String` URL, the SDK will ignore the Google In-App Review Interaction
     * in favor of the Apptentive Rating Dialog Interaction, and will send the user to the
     * specified URL if the user chooses to rate the app.
     *
     * Amazon App Store "Amazon Music" app example:
     * Using package name - http://amazon.com/gp/mas/dl/android?p=com.amazon.mp3
     * Using ASIN         - http://amazon.com/gp/mas/dl/android?asin=B004FRX0MY
     */
    var customAppStoreURL: String? = null

    //region plugin configuration

    /**
     *
     * This field is intended for internal use only and should not be set by the app.
     *
     * The distributionName field identifies the type of platform on which the SDK is distributed.
     * By default, the value is set to "Default" for native SDKs.
     *
     * However, this value will be overridden by plugins with their respective values.
     * For instance, in the case of the mParticle plugin, the value should be set to "mParticle".
     *
     */

    @InternalUseOnly var distributionName: String = "Default"

    /**
     * This field is intended for internal use only and should not be set by the app.
     *
     * The distributionVersion field identifies the current version of the SDK.
     * For native SDKs, the value is set to the current version of the SDK it is pointing at.
     *
     * However, this value will be overridden by plugins with their respective values.
     * For example, in the case of the mParticle plugin,
     * the value should be set to the current version of the mParticle SDK
     *
     */

    @InternalUseOnly var distributionVersion: String = Constants.SDK_VERSION

    //endregion

    init {
        require(apptentiveKey.isNotEmpty()) { "apptentiveKey is null or empty" }
        require(apptentiveSignature.isNotEmpty()) { "apptentiveSignature is null or empty" }
    }
}
