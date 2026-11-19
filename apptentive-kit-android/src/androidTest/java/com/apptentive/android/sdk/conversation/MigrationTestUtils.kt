package com.apptentive.android.sdk.conversation

import android.content.Context
import java.io.File

object MigrationTestUtils {
    fun clearDeviceStorage(context: Context) {
        // Recursively remove every file under the app's data dir so each migration test starts from a
        // clean slate before the legacy fixtures for a given SDK version are seeded.
        deleteFile(context.dataDir)
    }

    private fun deleteFile(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteFile(it) }
        } else {
            file.delete()
        }
    }

    fun getInternalDir(context: Context, path: String, createIfNecessary: Boolean = false): File {
        val filesDir = context.filesDir
        val internalDir = File(filesDir, path)
        if (!internalDir.exists() && createIfNecessary) {
            val succeed = internalDir.mkdirs()
            if (!succeed) {
                throw AssertionError("Unable to create directory: $path")
            }
        }
        return internalDir
    }
}
