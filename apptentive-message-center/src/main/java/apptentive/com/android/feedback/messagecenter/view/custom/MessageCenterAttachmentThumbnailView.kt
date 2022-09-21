package apptentive.com.android.feedback.messagecenter.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.ImageUtil
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView

internal class MessageCenterAttachmentThumbnailView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.apptentive_attachment_item, this)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return Button::class.java.name
    }

    fun setAttachmentView(file: Message.Attachment, isDownloading: Boolean, onClickAttachment: () -> Unit) {
        try {
            when {
                file.hasLocalFile() -> {
                    if (FileUtil.isMimeTypeImage(file.contentType)) showImageThumbnail(file)
                    else showNonImageThumbnail(file)
                }
                else -> showDownloadableThumbnail(file)
            }
        } catch (e: Exception) {
            showNonImageThumbnail(file)
        }

        // Will be true if there is no local file and the user has tapped the thumbnail
        val progressIndicator = findViewById<CircularProgressIndicator>(R.id.apptentive_attachment_thumbnail_download_loading)
        progressIndicator.isVisible = isDownloading

        /**
         * Will do 1 of 3 things.
         * 1. If draft, show options to preview or remove
         * 2. If has a local file, preview the image full screen
         * 3. If no local file, download the file, create a local file, then replace thumbnail
         */
        val fileLayout = findViewById<ConstraintLayout>(R.id.apptentive_attachment_item)
        fileLayout.setOnClickListener {
            onClickAttachment()
        }
    }

    private fun showImageThumbnail(file: Message.Attachment) {
        val attachmentThumbnail = findViewById<ImageView>(R.id.apptentive_attachment_thumbnail)
        val bitmapThumbnail = ImageUtil.getImageThumbnailBitmap(file.localFilePath)
        attachmentThumbnail.setImageBitmap(bitmapThumbnail)
    }

    private fun showNonImageThumbnail(file: Message.Attachment) {
        Log.d(MESSAGE_CENTER, "Non image or issue creating image thumbnail. Using generic document icon.")
        val attachmentMimeType = findViewById<MaterialTextView>(R.id.apptentive_attachment_mime_text)
        attachmentMimeType.text = MimeTypeMap.getSingleton().getExtensionFromMimeType(file.contentType).orEmpty()
    }

    private fun showDownloadableThumbnail(file: Message.Attachment) {
        val attachmentMimeType = findViewById<MaterialTextView>(R.id.apptentive_attachment_mime_text)
        attachmentMimeType.text = MimeTypeMap.getSingleton().getExtensionFromMimeType(file.contentType).orEmpty()
        val downloadIcon = findViewById<ImageView>(R.id.apptentive_attachment_thumbnail_download_image)
        downloadIcon.isVisible = !file.hasLocalFile()
    }
}
