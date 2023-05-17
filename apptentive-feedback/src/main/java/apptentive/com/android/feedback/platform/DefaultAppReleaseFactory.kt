package apptentive.com.android.feedback.platform

import android.content.Context
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.utils.RuntimeUtils
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.Factory

internal class DefaultAppReleaseFactory(
    private val context: Context
) : Factory<AppRelease> {
    override fun create(): AppRelease {
        val applicationInfo = RuntimeUtils.getApplicationInfo(context)
        val sharedPrefsURL = context.getSharedPreferences(SharedPrefConstants.CUSTOM_STORE_URL, Context.MODE_PRIVATE)
        val customAppStoreURL = sharedPrefsURL.getString(SharedPrefConstants.CUSTOM_STORE_URL_KEY, null)
        val sharedPrefsTheme = context.getSharedPreferences(SharedPrefConstants.USE_HOST_APP_THEME, Context.MODE_PRIVATE)
        val shouldInheritStyle = sharedPrefsTheme.getBoolean(SharedPrefConstants.USE_HOST_APP_THEME_KEY, true)

        return AppRelease(
            type = "android",
            identifier = applicationInfo.packageName,
            versionCode = applicationInfo.versionCode,
            versionName = applicationInfo.versionName,
            targetSdkVersion = applicationInfo.targetSdkVersion.toString(),
            minSdkVersion = applicationInfo.minSdkVersion.toString(),
            debug = applicationInfo.debuggable,
            inheritStyle = shouldInheritStyle,
            overrideStyle = !shouldInheritStyle,
            appStore = if (customAppStoreURL == null) "Google" else null,
            customAppStoreURL = customAppStoreURL
        )
    }
}
