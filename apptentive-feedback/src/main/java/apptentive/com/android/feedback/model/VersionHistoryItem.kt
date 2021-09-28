package apptentive.com.android.feedback.model

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
data class VersionHistoryItem(
    val timestamp: Double,
    val versionCode: VersionCode,
    val versionName: VersionName
)
