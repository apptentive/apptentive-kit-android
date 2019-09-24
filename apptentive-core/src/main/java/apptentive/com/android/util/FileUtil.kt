package apptentive.com.android.util

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.util.LogTags.core
import java.io.File

object FileUtil {
    @WorkerThread
    fun getInternalDir(context: Context, path: String, createIfNecessary: Boolean = false): File {
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