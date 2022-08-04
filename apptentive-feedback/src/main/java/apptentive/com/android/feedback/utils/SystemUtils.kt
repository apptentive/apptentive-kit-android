package apptentive.com.android.feedback.utils

import android.content.Context
import android.content.pm.PackageManager

internal object SystemUtils {
    fun hasPermission(context: Context, permission: String): Boolean {
        val perm = context.checkCallingOrSelfPermission(permission)
        return perm == PackageManager.PERMISSION_GRANTED
    }
}
