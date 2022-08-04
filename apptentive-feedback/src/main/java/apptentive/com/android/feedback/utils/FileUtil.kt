package apptentive.com.android.feedback.utils

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.URLUtil
import androidx.annotation.WorkerThread
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.model.StoredFile
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.UTIL
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

@InternalUseOnly
internal object FileUtil {
    private val fileSystem: FileSystem by lazy { DependencyProvider.of<FileSystem>() }

    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File {
        return fileSystem.getInternalDir(path, createIfNecessary)
    }

    fun getMimeTypeFromUri(context: Context, contentUri: Uri): String? {
        return context.contentResolver?.getType(contentUri) // Usually `application/TYPE`
    }

    fun generateCacheFilePathFromNonceOrPrefix(nonce: String, prefix: String?): String {
        val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext().getAppActivity()

        val fileName = prefix ?: "apptentive-api-file-$nonce"
        val cacheDir = getDiskCacheDir(activity)
        val cacheFile = File(cacheDir, fileName)
        return cacheFile.path
    }

    private fun getDiskCacheDir(activity: Activity): File? {
        return if ((Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) &&
            SystemUtils.hasPermission(activity, WRITE_EXTERNAL_STORAGE)
        ) activity.externalCacheDir else activity.cacheDir ?: null
    }

    /**
     * This method creates a cached file exactly copying from the input stream.
     *
     * @param sourceUri     the source file path or uri string
     * @param localFilePath the cache file path string
     * @param mimeType      the mimeType of the source input stream
     * @return null if failed, otherwise a StoredFile object
     */
    fun createLocalStoredFile(sourceUri: String, localFilePath: String, mimeType: String?): StoredFile? {
        val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext().getAppActivity()
        var inputStream: InputStream? = null
        return try {
            val uri = Uri.parse(sourceUri)
            inputStream = if (URLUtil.isContentUrl(sourceUri)) {
                activity.contentResolver.openInputStream(uri)
            } else {
                val file = File(uri.path ?: sourceUri)
                FileInputStream(file)
            }
            createLocalStoredFile(inputStream, sourceUri, localFilePath, mimeType)
        } catch (e: FileNotFoundException) {
            null
        } finally {
            ensureClosed(inputStream)
        }
    }

    /**
     * This method creates a cached file copy from the source input stream.
     *
     * @param inputStream   the source input stream
     * @param sourceUri     the source file path or uri string
     * @param localFilePath the cache file path string
     * @param mimeType      the mimeType of the source input stream
     * @return null if failed, otherwise a StoredFile object
     */
    fun createLocalStoredFile(
        inputStream: InputStream?,
        sourceUri: String,
        localFilePath: String,
        mimeType: String?
    ): StoredFile? {
        if (inputStream == null) return null

        // Copy the file contents over.
        var cos: CountingOutputStream? = null
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        try {
            val localFile = File(localFilePath)
            // Local cache file name may not be unique, and can be reused, in which case, the
            //  previously created cache file need to be deleted before it is being copied over.
            if (localFile.exists()) localFile.delete()
            fos = FileOutputStream(localFile)
            bos = BufferedOutputStream(fos)
            cos = CountingOutputStream(bos)
            val buf = ByteArray(2048)
            var count: Int
            while (inputStream.read(buf, 0, 2048).also { count = it } != -1) {
                cos.write(buf, 0, count)
            }
            Log.v(UTIL, "File saved, size = " + (cos.bytesWritten / 1024).toString() + "k")
        } catch (e: IOException) {
            Log.e(UTIL, "Error creating local copy of file attachment.", e)
            return null
        } finally {
            ensureClosed(cos)
            ensureClosed(bos)
            ensureClosed(fos)
        }

        // Create a StoredFile database entry for this locally saved file.
        return StoredFile(
            sourceUriOrPath = sourceUri,
            localFilePath = localFilePath,
            mimeType = mimeType
        )
    }

    fun isMimeTypeImage(mimeType: String?): Boolean {
        if (mimeType.isNullOrBlank()) return false

        val fileType = mimeType.substring(0, mimeType.indexOf("/"))
        return fileType.equals("Image", ignoreCase = true)
    }

    fun appendFileToStream(fileInputStream: InputStream, outputStream: OutputStream) {
        try {
            copy(fileInputStream, outputStream)
        } catch (e: Exception) {
            Log.e(UTIL, "Exception while appending file to stream", e)
        }
    }

    fun ensureClosed(stream: Closeable?) {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                Log.e(UTIL, "Exception while closing stream", e)
            }
        }
    }

    private fun copy(input: InputStream, output: OutputStream) {
        try {
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } > 0) {
                output.write(buffer, 0, bytesRead)
            }
        } catch (e: Exception) {
            Log.e(UTIL, "Exception while copying stream", e)
        }
    }

    fun getFileName(uriPath: String, mimeType: String?): String {
        val defaultName = "file.$mimeType"

        return try {
            val contentResolver = DependencyProvider.of<EngagementContextFactory>()
                .engagementContext().getAppActivity().contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                contentResolver.query(Uri.parse(uriPath), null, null, null)?.use {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    it.getString(if (nameIndex >= 0) nameIndex else 0).replace(",", "")
                } ?: defaultName
            } else defaultName
        } catch (e: Exception) {
            Log.d(UTIL, "Exception while retrieving name, using default: $defaultName")
            defaultName
        }
    }
}
