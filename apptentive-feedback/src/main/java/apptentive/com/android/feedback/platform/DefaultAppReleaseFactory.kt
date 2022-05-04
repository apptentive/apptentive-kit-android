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
        val sharedPrefs = context.getSharedPreferences(SharedPrefConstants.CUSTOM_STORE_URL, Context.MODE_PRIVATE)
        val customAppStoreURL = sharedPrefs.getString(SharedPrefConstants.CUSTOM_STORE_URL_KEY, null)

        return AppRelease(
            type = "android",
            identifier = applicationInfo.packageName,
            versionCode = applicationInfo.versionCode,
            versionName = applicationInfo.versionName,
            targetSdkVersion = applicationInfo.targetSdkVersion.toString(),
            minSdkVersion = applicationInfo.minSdkVersion.toString(),
            debug = applicationInfo.debuggable,
            inheritStyle = false,
            overrideStyle = false,
            appStore = null,
            customAppStoreURL = customAppStoreURL
        )
    }
}
