package apptentive.com.android.util

import androidx.annotation.WorkerThread
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.FileSystem
import java.io.File

object FileUtil {
    private val fileSystem: FileSystem by lazy {
        DependencyProvider.of<FileSystem>()
    }

    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File {
        return fileSystem.getInternalDir(path, createIfNecessary)
    }
}
