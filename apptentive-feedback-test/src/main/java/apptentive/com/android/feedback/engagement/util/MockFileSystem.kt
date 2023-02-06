package apptentive.com.android.feedback.engagement.util

import apptentive.com.android.feedback.platform.FileSystem
import io.mockk.every
import io.mockk.mockk
import java.io.File

class MockFileSystem(private val containsFile: Boolean = true) : FileSystem {
    override fun getInternalDir(path: String, createIfNecessary: Boolean): File {
        val file: File = mockk() {
            every { exists() } returns true
            every { isDirectory } returns false
            every { name } returns ""
            every { delete() } returns false
        }
        return file
    }

    override fun containsFile(path: String): Boolean {
        return containsFile
    }
}
