package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class AppRelease(
    val type: String,
    val identifier: String,
    val versionCode: VersionCode,
    val versionName: VersionName,
    val targetSdkVersion: String,
    val debug: Boolean = false,
    val inheritStyle: Boolean = false,
    val overrideStyle: Boolean = false,
    val appStore: String? = null
)