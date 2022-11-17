package apptentive.com.android.feedback.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
object SystemUtils {
    fun hasPermission(context: Context, permission: String): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(permission)
        } else context.checkCallingOrSelfPermission(permission)
        return perm == PackageManager.PERMISSION_GRANTED
    }
}
