package apptentive.com.android.feedback.platform

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CORE

@InternalUseOnly
@SuppressLint("QueryPermissionsNeeded")
fun Context.tryStartActivity(intent: Intent): Boolean {
    try {
        val packageManager = packageManager
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
