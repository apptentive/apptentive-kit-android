package apptentive.com.android.feedback.utils

import androidx.annotation.WorkerThread
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.util.InternalUseOnly
import java.io.File

@InternalUseOnly
internal object FileUtil {
    private val fileSystem: FileSystem by lazy {
        DependencyProvider.of<FileSystem>()
    }

    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File {
        return fileSystem.getInternalDir(path, createIfNecessary)
    }
}
