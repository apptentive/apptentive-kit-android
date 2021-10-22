package apptentive.com.android.feedback.platform

import android.content.Context
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.utils.RuntimeUtils
import apptentive.com.android.util.Factory

internal class DefaultAppReleaseFactory(
    private val context: Context
) : Factory<AppRelease> {
    override fun create(): AppRelease {
        val applicationInfo = RuntimeUtils.getApplicationInfo(context)
        return AppRelease(
            type = "android",
            identifier = applicationInfo.packageName,
            versionCode = applicationInfo.versionCode,
            versionName = applicationInfo.versionName,
            targetSdkVersion = applicationInfo.targetSdkVersion.toString(),
            debug = applicationInfo.debuggable,
            inheritStyle = false,
            overrideStyle = false,
            appStore = null
        )
    }
}
