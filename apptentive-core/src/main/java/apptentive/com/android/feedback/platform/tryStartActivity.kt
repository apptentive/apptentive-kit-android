package apptentive.com.android.feedback.platform

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core

@SuppressLint("QueryPermissionsNeeded")
fun Context.tryStartActivity(intent: Intent): Boolean {
    try {
        val packageManager = packageManager
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
    } catch (e: ActivityNotFoundException) {
        Log.e(core, "No activity found for intent: $intent", e)
    } catch (e: Exception) {
        Log.e(core, "Exception while starting activity for intent: $intent", e)
    }

    return false
}
