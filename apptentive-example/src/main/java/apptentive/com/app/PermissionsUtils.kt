package apptentive.com.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

fun openPermissionNeededDialog(context: Context, missingPermission: String, action: String) {
    AlertDialog.Builder(context).apply {
        setTitle("Permission needed")
        setMessage("$missingPermission permission is required to $action")
        setPositiveButton("Open settings") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(intent)
        }
        setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(context, "$missingPermission permission denied, cannot $action", Toast.LENGTH_LONG).show()
        }
    }.show()
}
