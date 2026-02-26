package apptentive.com.android.core

import android.content.Context
import android.os.Build
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface ApplicationInfo {
    val versionCode: Long
    val versionName: String
}

@InternalUseOnly
class AndroidApplicationInfo(context: Context) : ApplicationInfo {
    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    @Suppress("DEPRECATION")
    override val versionCode: Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
        else packageInfo.versionCode.toLong()
    override val versionName: String = packageInfo.versionName.toString()
}
