package apptentive.com.android.feedback.utils

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.UTIL
import apptentive.com.android.util.generateUUID
import java.io.BufferedInputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

@InternalUseOnly
object FileUtil {
    private val fileSystem: FileSystem by lazy { DependencyProvider.of<FileSystem>() }

    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File {
        return fileSystem.getInternalDir(path, createIfNecessary)
    }

    private fun getMimeTypeFromUri(context: Context, contentUri: Uri): String? {
        return context.contentResolver?.getType(contentUri) // Usually `application/TYPE`
    }

    fun generateCacheFilePathFromNonceOrPrefix(activity: Activity, nonce: String, prefix: String?): String {
        val fileName = prefix?.plus("-$nonce") ?: "apptentive-api-file-$nonce"
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
     * This method creates a cached file copying from the input stream.
     * Compresses if image type.
     *
     * @param uriString the local file path uri
     * @param nonce     the generated nonce of the message
     * @return null if failed, otherwise a [Message.Attachment]
     */
    fun createLocalStoredAttachment(activity: Activity, uriString: String, nonce: String): Message.Attachment? {
        val localFile = Uri.parse(uriString)

        var localFilePath = generateCacheFilePathFromNonceOrPrefix(
            activity,
            nonce,
            localFile?.lastPathSegment
        )
        var mimeType = getMimeTypeFromUri(activity, Uri.parse(uriString))
        val mime = MimeTypeMap.getSingleton()
        var extension = mime.getExtensionFromMimeType(mimeType)

        // If we can't get the mime type from the uri, try getting it from the extension.
        if (extension == null) extension = MimeTypeMap.getFileExtensionFromUrl(uriString)
        if (mimeType == null && extension != null) mimeType = mime.getMimeTypeFromExtension(extension)
        if (!extension.isNullOrEmpty()) localFilePath += ".$extension"

        var inputStream: InputStream? = null
        return try {
            inputStream = if (localFile != null) {
                activity.contentResolver.openInputStream(localFile)
            } else null
            createLocalStoredAttachmentFile(activity, inputStream, uriString, localFilePath, mimeType)
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
     * @param sourceUri     the local file path uri
     * @param localFilePath the cache file path string
     * @param mimeType      the mimeType of the source input stream
     * @return null if failed, otherwise a [Message.Attachment] object
     */
    fun createLocalStoredAttachmentFile(
        activity: Activity,
        inputStream: InputStream?,
        sourceUri: String,
        localFilePath: String,
        mimeType: String?
    ): Message.Attachment? {
        if (inputStream == null) return null

        var bis: BufferedInputStream? = null
        var cos: CountingOutputStream? = null
        var fos: FileOutputStream? = null
        val bytesWritten: Long
        try {
            val localFile = File(localFilePath)
            // Local cache file name may not be unique, and can be reused, in which case, the
            //  previously created cache file need to be deleted before it is being copied over.
            if (localFile.exists()) localFile.delete()
            bis = BufferedInputStream(inputStream)
            fos = FileOutputStream(localFile)
            cos = CountingOutputStream(fos)

            System.gc() // Clear up memory to help prevent OOM issues

            if (isMimeTypeImage(mimeType)) {
                val smallerImage = ImageUtil.createScaledBitmapFromLocalImageSource(activity, inputStream, sourceUri)
                smallerImage?.compress(Bitmap.CompressFormat.JPEG, 95, cos)
                Log.v(UTIL, "New image file size = " + (cos.bytesWritten / 1024).toString() + "k")
                smallerImage?.recycle()
            } else bis.use { cos.write(it.read()) }
            bytesWritten = cos.bytesWritten
            Log.v(UTIL, "File saved, size = " + (cos.bytesWritten / 1024).toString() + "k")
        } catch (e: IOException) {
            Log.e(UTIL, "Error creating local copy of file attachment.", e)
            return null
        } finally {
            cos?.flush()
            ensureClosed(bis)
            ensureClosed(cos)
            ensureClosed(fos)
            System.gc() // Clean up memory after done
        }

        // Create a Attachment database entry for this locally saved file.
        return Message.Attachment(
            id = generateUUID(),
            sourceUriOrPath = sourceUri,
            localFilePath = localFilePath,
            contentType = mimeType,
            size = bytesWritten
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
        } finally {
            ensureClosed(fileInputStream)
            ensureClosed(outputStream)
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun writeFileData(fileLocation: String, data: ByteArray) {
        val fileOutputStream = FileOutputStream(File(fileLocation))
        try {
            fileOutputStream.use { outputStream -> outputStream.write(data) }
        } catch (e: Exception) {
            Log.e(UTIL, "Exception writing file", e)
        } finally {
            ensureClosed(fileOutputStream)
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun readFileData(fileLocation: String): ByteArray {
        try {
            return readFile(File(fileLocation))
        } catch (e: Exception) {
            Log.e(UTIL, "Exception reading file", e)
            throw e
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun readFile(file: File): ByteArray {
        val fileSize = verifyFileSize(file)
        val data = ByteArray(fileSize)
        val read = readFile(file, data)
        verifyAllDataRead(file, data, read)
        return data
    }

    @Throws(IOException::class)
    private fun verifyFileSize(file: File): Int {
        val fileSize = file.length()
        if (fileSize > Int.MAX_VALUE) {
            throw IOException("File size (" + fileSize + " bytes) for " + file.name + " too large.")
        }
        return fileSize.toInt()
    }

    @Throws(FileNotFoundException::class, IOException::class)
    private fun readFile(file: File, data: ByteArray): Int {
        val fileInputStream = FileInputStream(file)
        try {
            fileInputStream.use { inputStream -> return inputStream.read(data) }
        } catch (e: Exception) {
            Log.e(UTIL, "Exception reading file", e)
            throw e
        } finally {
            ensureClosed(fileInputStream)
        }
    }

    @Throws(IOException::class)
    fun verifyAllDataRead(file: File, data: ByteArray, read: Int) {
        if (read != data.size) {
            throw IOException(
                "Expected to read " + data.size +
                    " bytes from file " + file.name + " but got only " + read + " bytes from file."
            )
        }
    }

    fun ensureClosed(stream: Closeable?) {
        if (stream != null) {
            try {
                if (stream is OutputStream) stream.flush()
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

    fun deleteFile(filePath: String?) {
        if (!filePath.isNullOrBlank()) {
            val file = File(filePath)
            if (file.exists()) file.delete()
        }
    }
}
