package apptentive.com.android.core

import android.content.Context
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface ApplicationInfo {
    val versionCode: Int
    val versionName: String
}

@InternalUseOnly
class AndroidApplicationInfo(context: Context) : ApplicationInfo {
    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    @Suppress("DEPRECATION")
    override val versionCode: Int = packageInfo.versionCode
    override val versionName: String = packageInfo.versionName
}
