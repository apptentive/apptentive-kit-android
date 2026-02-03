package apptentive.com.android.feedback.utils

import android.content.Context
import android.os.Build

internal object RuntimeUtils {
    fun getApplicationInfo(context: Context): ApplicationInfo {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val ai = packageInfo.applicationInfo
        val debuggable =
            ai != null && ai.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        val targetSdkVersion = ai?.targetSdkVersion ?: 0

        val minSdkVersion: Int =
            ai?.minSdkVersion ?: 0
        @Suppress("DEPRECATION")
        return ApplicationInfo(
            packageName = context.packageName,
            versionName = packageInfo.versionName.toString(),
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
            else packageInfo.versionCode.toLong(),
            targetSdkVersion = targetSdkVersion,
            minSdkVersion = minSdkVersion,
            debuggable = debuggable
        )
    }
}

internal typealias VersionCode = Long
internal typealias VersionName = String

internal data class ApplicationInfo(
    val packageName: String,
    val versionName: VersionName,
    val versionCode: VersionCode,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val debuggable: Boolean
)
