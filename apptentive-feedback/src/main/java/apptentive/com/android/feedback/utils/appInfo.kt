package apptentive.com.android.feedback.utils

import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getApplicationInfo

internal val appVersionCode by lazy { getApplicationInfo().versionCode }
internal val appVersionName by lazy { getApplicationInfo().versionName }
