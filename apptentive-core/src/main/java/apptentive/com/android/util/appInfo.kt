package apptentive.com.android.util

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ApplicationInfo

val appVersionCode by lazy { DependencyProvider.of<ApplicationInfo>().versionCode }
val appVersionName by lazy { DependencyProvider.of<ApplicationInfo>().versionName }