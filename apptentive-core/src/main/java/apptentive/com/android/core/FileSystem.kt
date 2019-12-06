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

class AndroidFileSystemProvider(context: Context, private val domain: String) :
    Provider<FileSystem> {
    private val applicationContext = context.applicationContext

    override fun get(): FileSystem {
        return AndroidFileSystem(applicationContext, domain)
    }
}

private class AndroidFileSystem(
    private val applicationContext: Context,
    private val domain: String
) : FileSystem {
    override fun getInternalDir(path: String, createIfNecessary: Boolean): File {
        val internalDir = File(applicationContext.filesDir, "$domain/$path")
        if (!internalDir.exists() && createIfNecessary) {
            val succeed = internalDir.mkdirs()
            if (!succeed) {
                Log.w(core, "Unable to create internal directory: $internalDir")
            }
        }
        return internalDir
    }
}