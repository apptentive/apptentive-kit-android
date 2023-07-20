package apptentive.com.app

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

fun openFileAsset(context: Context, path: String): InputStream? {
    val assetManager = context.resources.assets
    try {
        return BufferedInputStream(assetManager.open(path))
    } catch (e: IOException) {
        Log.e("APPTENTIVE", "Error open stream from file \"$path\"", e)
    }
    return null
}

fun createFileAssetUriString(path: String): String {
    return "file:///android_asset/$path"
}

fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
    return contentResolver.query(uri, null, null, null)?.use {
        it.moveToFirst()
        it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }.orEmpty()
}

fun createImageFile(context: Context): File? {
    val timeStamp = SimpleDateFormat.getDateTimeInstance().format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    try {
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    } catch (ex: IOException) {
        Toast.makeText(context, "Image Creation Error", Toast.LENGTH_LONG).show()
    }

    return null
}
