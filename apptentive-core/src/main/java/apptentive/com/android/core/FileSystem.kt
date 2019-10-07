package apptentive.com.android.core

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core
import java.io.File

interface FileSystem {
    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File
}

class AndroidFileSystemProvider(context: Context) : Provider<FileSystem> {
    private val context = context.applicationContext

    override fun get(): FileSystem {
        return AndroidFileSystem(context)
    }
}

private class AndroidFileSystem(private val context: Context) : FileSystem {
    override fun getInternalDir(path: String, createIfNecessary: Boolean): File {
        val internalDir = File(context.filesDir, path)
        if (!internalDir.exists() && createIfNecessary) {
            val succeed = internalDir.mkdirs()
            if (!succeed) {
                Log.w(core, "Unable to create internal directory: $internalDir")
            }
        }
        return internalDir
    }
}