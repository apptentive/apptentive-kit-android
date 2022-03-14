package apptentive.com.android.feedback.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object RuntimeUtils {
    @Suppress("DEPRECATION")
    fun getApplicationInfo(context: Context): ApplicationInfo {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val ai = packageInfo.applicationInfo
        val debuggable =
            ai != null && ai.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        val targetSdkVersion = ai?.targetSdkVersion ?: 0

        val minSdkVersion: Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) getMinSdkVersion(ai)
            else 0
        return ApplicationInfo(
            packageName = context.packageName,
            versionName = packageInfo.versionName,
            versionCode = packageInfo.versionCode.toLong(),
            targetSdkVersion = targetSdkVersion,
            minSdkVersion = minSdkVersion,
            debuggable = debuggable
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getMinSdkVersion(ai: android.content.pm.ApplicationInfo?) = ai?.minSdkVersion ?: 0
}

internal typealias VersionCode = Long
internal typealias VersionName = String

data class ApplicationInfo(
    val packageName: String,
    val versionName: VersionName,
    val versionCode: VersionCode,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val debuggable: Boolean
)
