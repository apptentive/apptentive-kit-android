package apptentive.com.android.feedback.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Size
import android.webkit.URLUtil
import androidx.exifinterface.media.ExifInterface
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.UTIL
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import kotlin.math.min

@InternalUseOnly
object ImageUtil {
    private const val MAX_SENT_IMAGE_EDGE = 2048
    private const val THUMBNAIL_SIZE = 256

    /**
     * This method first uses a straight binary pixel conversion to shrink an image to *almost* the
     * right size, and then performs a scaling of this resulting bitmap to achieve the final size.
     * It will create two bitmaps in memory while it is running.
     *
     * @param inputStream Stream to read original image
     * @param imageUri    Either full absolute path to the source image file or the content uri to the source image
     * @return A Bitmap scaled by maxWidth, maxHeight, and config.
     */
    @Synchronized
    @Throws(NullPointerException::class, FileNotFoundException::class)
    fun createScaledBitmapFromLocalImageSource(
        activity: Activity,
        inputStream: InputStream,
        imageUri: String
    ): Bitmap? {
        val exif = ExifInterface(inputStream)
        val imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val tempBitmap = createTempBitmap(activity, imageUri, imageOrientation) ?: return null

        // Start by grabbing the bitmap from file, sampling down a little first if the image is huge.
        var outBitmap = tempBitmap
        val width = tempBitmap.width
        val height = tempBitmap.height

        // Find the greatest ration difference, as this is what we will shrink both sides to.
        val ratio: Float = calculateBitmapScaleFactor(width, height)
        if (ratio < 1.0f) { // Don't blow up small images, only shrink bigger ones.
            val newWidth = (ratio * width).toInt()
            val newHeight = (ratio * height).toInt()
            Log.v(UTIL, "Scaling image further down to $newWidth x $newHeight")
            outBitmap = try {
                Bitmap.createScaledBitmap(tempBitmap, newWidth, newHeight, true)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Failed to create scaled bitmap")
            }
            Log.v(UTIL, "Final bitmap dimensions: ${outBitmap.width} x ${outBitmap.height}")
            tempBitmap.recycle()
        }
        return outBitmap
    }

    private fun createTempBitmap(activity: Activity, filePath: String, orientation: Int): Bitmap? {
        return if (URLUtil.isContentUrl(filePath)) {
            try {
                val uri = Uri.parse(filePath)
                createLightweightScaledBitmap(activity, null, uri, orientation)
            } catch (e: NullPointerException) {
                throw NullPointerException("Failed to create scaled bitmap")
            }
        } else {
            if (File(filePath).exists()) {
                try {
                    createLightweightScaledBitmap(activity, filePath, null, orientation)
                } catch (e: NullPointerException) {
                    throw NullPointerException("Failed to create scaled bitmap")
                }
            } else {
                throw FileNotFoundException("Source file does not exist any more")
            }
        }
    }

    /**
     * This method decodes a bitmap from a file, and does pixel combining in order to produce an in-memory
     * bitmap that is smaller than the original. It will create only the returned bitmap in memory.
     * From [Loading Large Bitmaps Efficiently](http://developer.android.com/training/displaying-bitmaps/load-bitmap.html)
     *
     * @param filePath     Full absolute path to the image file. (optional, maybe null)
     * @param fileUri      Content uri of the source image. (optional, maybe null)
     * @param orientation  The orientation for the image expressed as degrees
     * @return A bitmap whose edges are equal to or less than MAX_SENT_IMAGE_EDGE in length.
     */
    @Throws(NullPointerException::class, FileNotFoundException::class)
    private fun createLightweightScaledBitmap(
        activity: Activity,
        filePath: String?,
        fileUri: Uri?,
        orientation: Int
    ): Bitmap? {
        val decodeBitmapFromUri = when {
            fileUri != null -> true
            !filePath.isNullOrEmpty() -> false
            else -> return null
        }
        val decodeBoundsOptions = BitmapFactory.Options()
        decodeBoundsOptions.inJustDecodeBounds = true
        decodeBoundsOptions.inScaled = false

        // Obtain image dimensions without actually decode the image into memory
        if (decodeBitmapFromUri) {
            var inputStream: InputStream? = null
            try {
                inputStream =
                    fileUri?.let { activity.contentResolver.openInputStream(it) }
                BitmapFactory.decodeStream(inputStream, null, decodeBoundsOptions)
            } catch (e: FileNotFoundException) {
                throw FileNotFoundException("Failed to decode image")
            } finally {
                FileUtil.ensureClosed(inputStream)
            }
        } else if (!decodeBitmapFromUri) {
            BitmapFactory.decodeFile(filePath, decodeBoundsOptions)
        }
        val width: Int
        val height: Int
        if (orientation == 90 || orientation == 270) {
            width = decodeBoundsOptions.outHeight
            height = decodeBoundsOptions.outWidth
        } else {
            width = decodeBoundsOptions.outWidth
            height = decodeBoundsOptions.outHeight
        }
        Log.v(UTIL, "Original bitmap dimensions: $width x $height")

        // Set options to return smaller image if necessary
        val sampleRatio = min(width / MAX_SENT_IMAGE_EDGE, height / MAX_SENT_IMAGE_EDGE)
        val options = BitmapFactory.Options()
        if (sampleRatio >= 2) {
            options.inSampleSize = sampleRatio
        }
        options.inScaled = false
        options.inJustDecodeBounds = false
        Log.v(UTIL, "Bitmap sample size = ${options.inSampleSize}")

        // Decode the image with options
        var returnImage: Bitmap? = null
        if (decodeBitmapFromUri) {
            var inputStream: InputStream? = null
            try {
                inputStream = fileUri?.let { activity.contentResolver.openInputStream(it) }
                returnImage = BitmapFactory.decodeStream(inputStream, null, options)
            } catch (e: FileNotFoundException) {
                throw FileNotFoundException("Failed to decode image")
            } finally {
                FileUtil.ensureClosed(inputStream)
            }
        } else if (!decodeBitmapFromUri) {
            returnImage = BitmapFactory.decodeFile(filePath, options)
        }
        Log.v(UTIL, "Sampled bitmap size = ${options.outWidth} X ${options.outHeight}")

        // Reorient if needed
        if (orientation != 0 && orientation != -1 && returnImage != null) {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            returnImage = try {
                Bitmap.createBitmap(
                    returnImage, 0, 0, returnImage.width,
                    returnImage.height, matrix, true
                )
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Failed to decode image", e)
            }
        }
        if (returnImage == null) {
            throw NullPointerException("Failed to decode image")
        }
        return returnImage
    }

    private fun calculateBitmapScaleFactor(width: Int, height: Int): Float {
        val widthRatio =
            if (MAX_SENT_IMAGE_EDGE <= 0) 1.0f else MAX_SENT_IMAGE_EDGE.toFloat() / width
        val heightRatio =
            if (MAX_SENT_IMAGE_EDGE <= 0) 1.0f else MAX_SENT_IMAGE_EDGE.toFloat() / height
        return min(1.0f, min(widthRatio, heightRatio)) // Don't scale above 1.0x
    }

    fun getImageThumbnailBitmap(filePath: String?): Bitmap? {
        return try {
            if (!filePath.isNullOrBlank()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ThumbnailUtils.createImageThumbnail(
                        File(filePath),
                        Size(THUMBNAIL_SIZE, THUMBNAIL_SIZE),
                        null
                    )
                } else {
                    ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(filePath),
                        THUMBNAIL_SIZE,
                        THUMBNAIL_SIZE
                    )
                }
            } else throw FileNotFoundException("File path is empty")
        } catch (e: Exception) {
            throw e
        }
    }

    fun loadAvatar(imageUrl: String): Bitmap? {
        var avatarBitmap: Bitmap? = null
        try {
            val url = URL(imageUrl)
            avatarBitmap = BitmapFactory.decodeStream(url.openStream())
        } catch (e: Exception) {
            Log.w(MESSAGE_CENTER, "Cannot fetch Avatar image due to $e")
        }
        return avatarBitmap
    }
}
