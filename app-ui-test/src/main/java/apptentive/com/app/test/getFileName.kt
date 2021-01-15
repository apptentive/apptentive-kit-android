package apptentive.com.app.test

import java.io.File

fun getFileName(path: String): String {
    val pos = path.lastIndexOf(File.pathSeparatorChar)
    return path.substring(pos + 1)
}