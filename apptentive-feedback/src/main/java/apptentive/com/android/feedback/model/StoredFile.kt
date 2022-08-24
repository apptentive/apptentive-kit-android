package apptentive.com.android.feedback.model

import android.webkit.MimeTypeMap
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.utils.FileUtil

data class StoredFile(
    var id: String? = null,
    var mimeType: String? = null,

    /*
    * For outgoing attachment, this field is the source image uri or source image full path
	* if READ_EXTERNAL_STORAGE permission is granted
	* For incoming attachment, this field is the full path to the on-device cache file where it is downloaded to
	*/
    var sourceUriOrPath: String,

    /*
    * For outgoing attachment, this field is the full path to the on-device cache file where the source image is copied to.
    * For incoming attachment, this field is the full path to the on-device cache file where the thumbnail is downloaded to.
    */
    var localFilePath: String,

    /*
    * For outgoing attachment, this field is empty.
    * For incoming attachment, this field is the full remote url to the attachment
    */
    var apptentiveUri: String = "",

    // Creation time of original file; set to 0 if failed to retrieve creation time from original uri
    var creationTime: TimeInterval = toSeconds(System.currentTimeMillis()),

    // Will either be the actual file name, or `file.mimeTypeExtension`
    val fileName: String = FileUtil.getFileName(
        sourceUriOrPath,
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    ),

    /*
    * Bytes of saved file. Divide by 1024 to get kb. Divide again by 1024 to get mb.
    */
    val fileSize: Long
)
