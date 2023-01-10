package apptentive.com.android.feedback.platform

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.core.Provider
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CORE
import java.io.File

@InternalUseOnly
interface FileSystem {
    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File

    fun containsFile(path: String): Boolean
}

internal class AndroidFileSystemProvider(context: Context, private val domain: String) :
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
                Log.w(CORE, "Unable to create internal directory: $internalDir")
            }
        }
        return internalDir
    }

    override fun containsFile(path: String): Boolean {
        val internalDir = File(applicationContext.filesDir, "$domain/$path")
        return internalDir.exists() && (internalDir.listFiles()?.size ?: 0) > 0
    }
}
