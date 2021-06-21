package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class VersionHistoryItem(
    val timestamp: Double,
    val versionCode: VersionCode,
    val versionName: VersionName
)
