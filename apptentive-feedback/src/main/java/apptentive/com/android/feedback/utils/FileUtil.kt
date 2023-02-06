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
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
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
    private const val ONE_MB = 1024 * 1024 // 1MB
    private const val MAX_FILE_SIZE = 15 * ONE_MB // 15MB
    private const val BYTES_BUFFER = ONE_MB // 1MB

    @WorkerThread
    fun getInternalDir(path: String, createIfNecessary: Boolean = false): File {
        return fileSystem.getInternalDir(path, createIfNecessary)
    }

    fun containsFiles(path: String): Boolean = fileSystem.containsFile(path)

    private fun getMimeTypeFromUri(context: Context, contentUri: Uri): String? {
        return context.contentResolver?.getType(contentUri) // Usually `application/TYPE`
    }

    fun generateCacheFilePathFromNonceOrPrefix(activity: Context, nonce: String, prefix: String?): String {
        val fileName = prefix?.plus("-$nonce") ?: "apptentive-api-file-$nonce"
        val cacheDir = getDiskCacheDir(activity)
        val cacheFile = File(cacheDir, fileName)
        return cacheFile.path
    }

    private fun getDiskCacheDir(activity: Context): File? {
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
     * @return `null` if failed, otherwise a [Message.Attachment]
     */
    fun createLocalStoredAttachment(activity: Activity, uriString: String, nonce: String): Message.Attachment? {
        val localFileUri = Uri.parse(uriString)

        var localFilePath = generateCacheFilePathFromNonceOrPrefix(
            activity,
            nonce,
            localFileUri?.lastPathSegment
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
            inputStream = if (localFileUri != null) {
                activity.contentResolver.openInputStream(localFileUri)
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
     * @return `null` if failed, otherwise a [Message.Attachment] object
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
        var fos: FileOutputStream? = null
        val fileSize: Long
        try {
            System.gc() // Clear up memory to help prevent OOM issues

            val localFile = File(localFilePath)
            // Local cache file name may not be unique, and can be reused, in which case, the
            //  previously created cache file need to be deleted before it is being copied over.
            if (localFile.exists()) localFile.delete()
            bis = BufferedInputStream(inputStream)
            fos = FileOutputStream(localFile)

            if (isMimeTypeImage(mimeType)) {
                val smallerImage = ImageUtil.createScaledBitmapFromLocalImageSource(activity, inputStream, sourceUri)
                smallerImage?.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                smallerImage?.recycle()
            } else bis.use { it.copyTo(fos) }

            fileSize = localFile.length()
            if (isValidFile(fileSize, mimeType)) Log.v(UTIL, "File successfully saved, size = ${(fileSize / 1024)}kb")
            else {
                localFile.delete()
                return null
            }
        } catch (e: IOException) {
            Log.e(UTIL, "Error creating local copy of file attachment.", e)
            return null
        } finally {
            ensureClosed(bis)
            ensureClosed(fos)
            System.gc() // Clean up memory after done
        }

        // Create an attachment database entry for this locally saved file.
        return Message.Attachment(
            id = generateUUID(),
            sourceUriOrPath = sourceUri,
            localFilePath = localFilePath,
            contentType = mimeType,
            size = fileSize,
            originalName = getFileName(activity, sourceUri, mimeType)
        )
    }

    fun isMimeTypeImage(mimeType: String?): Boolean {
        if (mimeType.isNullOrBlank()) return false

        val fileType = mimeType.substring(0, mimeType.indexOf("/"))
        return fileType.equals("Image", ignoreCase = true)
    }

    // https://android.googlesource.com/platform/external/mime-support/+/refs/heads/master/mime.types
    // Supported File types
    private const val IMAGE = "image"
    private const val AUDIO = "audio"
    private const val VIDEO = "video"
    private const val TEXT = "text" // Includes text files, html, csv, rtf, tsv, contact info, calendar
    private const val APPLICATION = "application"

    // Supported Application types
    // Documents
    private const val DOC = "doc"
    private const val DOCX = "docx"

    // PDF
    private const val PDF = "pdf"

    // Powerpoints
    private const val PPT = "ppt"
    private const val PPTX = "pptx"

    // Spreadsheets
    private const val XLS = "xls"
    private const val XLSX = "xlsx"

    private fun isValidMimeType(mimeType: String?): Boolean {
        Log.d(UTIL, "Looking for valid mime type for $mimeType")
        return when (mimeType?.substring(0, mimeType.indexOf("/"))) {
            IMAGE, AUDIO, VIDEO, TEXT -> true
            APPLICATION -> {
                when (MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)) {
                    DOC, DOCX, PDF, PPT, PPTX, XLS, XLSX -> true
                    else -> {
                        Log.e(UTIL, "Unable to find valid application type for mime type: $mimeType")
                        false
                    }
                }
            }
            else -> {
                Log.e(UTIL, "Unable to find valid type for mime type: $mimeType")
                false
            }
        }
    }

    private fun isValidFileSize(fileSizeBytes: Long): Boolean {
        val availableMemory = Runtime.getRuntime().freeMemory()
        Log.d(UTIL, "File size: ${fileSizeBytes / 1024} kb. Memory available: ${availableMemory / 1024} kb.")
        return if (fileSizeBytes <= availableMemory - BYTES_BUFFER && fileSizeBytes <= MAX_FILE_SIZE) true
        else {
            Log.e(UTIL, "File size too large: ${fileSizeBytes / 1024} kb. Memory available: ${availableMemory / 1024} kb. File size must be under 15MB")
            false
        }
    }

    private fun isValidFile(fileSize: Long, mimeType: String?): Boolean {
        return isValidMimeType(mimeType) && isValidFileSize(fileSize)
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
        val availableMemory = Runtime.getRuntime().freeMemory()
        Log.d(UTIL, "File size: ${fileSize / 1024} kb. Memory available: ${availableMemory / 1024} kb.")
        if (fileSize > MAX_FILE_SIZE && fileSize > availableMemory + BYTES_BUFFER) {
            throw IOException("${file.name} file size too large: ${fileSize / 1024} kb. Memory available: ${availableMemory / 1024} kb. File size must be under 15MB")
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

    fun deleteUnrecoverableStorageFiles(fileOrDirectory: File) {
        try {
            if (fileOrDirectory.isDirectory) {
                for (child in requireNotNull(fileOrDirectory.listFiles())) {
                    deleteUnrecoverableStorageFiles(child)
                }
            }
            Log.d(CONVERSATION, "File/directory to be deleted " + fileOrDirectory.name)
            fileOrDirectory.delete()
        } catch (e: java.lang.Exception) {
            Log.e(
                CONVERSATION,
                "Exception while trying to delete unrecoverable Conversation data files", e
            )
        }
    }

    private fun getFileName(activity: Activity, uriPath: String, mimeType: String?): String {
        val defaultName = "file.$mimeType"

        return try {
            val contentResolver = activity.contentResolver

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
