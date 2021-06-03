package com.apptentive.android.sdk.conversation

import android.content.Context
import java.io.File

object MigrationTestUtils {
    fun clearDeviceStorage(context: Context) {
        deleteFile(context.dataDir) // TODO: if this is not possible - delete specific directories
    }

    private fun deleteFile(file: File) {
        if (file.isDirectory) {
            file.listFiles().forEach { deleteFile(it) }
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