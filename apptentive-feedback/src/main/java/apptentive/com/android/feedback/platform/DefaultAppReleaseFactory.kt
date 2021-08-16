package apptentive.com.android.feedback.platform

import android.content.Context
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.utils.RuntimeUtils
import apptentive.com.android.util.Factory

// TODO: rename to AndroidAppReleaseFactory
class DefaultAppReleaseFactory(
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
            inheritStyle = false, // FIXME: set flag
            overrideStyle = false, // FIXME: set flag
            appStore = null // FIXME: set value
        )
    }
}
