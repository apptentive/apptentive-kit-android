package apptentive.com.android.core.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.CORE

internal fun Context.tryStartActivity(intent: Intent): Boolean {
    try {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
    } catch (e: ActivityNotFoundException) {
        Log.e(CORE, "No activity found for intent: $intent", e)
    } catch (e: Exception) {
        Log.e(CORE, "Exception while starting activity for intent: $intent", e)
    }

    return false
}
