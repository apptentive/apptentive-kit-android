package apptentive.com.android.feedback.utils

import apptentive.com.android.core.ApplicationInfo
import apptentive.com.android.core.DependencyProvider

internal val appVersionCode by lazy { DependencyProvider.of<ApplicationInfo>().versionCode }
internal val appVersionName by lazy { DependencyProvider.of<ApplicationInfo>().versionName }
