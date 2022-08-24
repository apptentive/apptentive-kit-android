package apptentive.com.android.feedback.messagecenter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.utils.ImageUtil
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import com.google.android.material.textview.MaterialTextView

class MessageCenterAttachmentThumbnailView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.apptentive_attachment_item, this)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return Button::class.java.name
    }

    fun setAttachmentView(filePath: String, mimeType: String?, onClickFile: () -> Unit) {
        try {
            val attachmentThumbnail = findViewById<ImageView>(R.id.apptentive_attachment_thumbnail)
            val bitmapThumbnail = ImageUtil.getImageThumbnailBitmap(filePath)
            attachmentThumbnail.setImageBitmap(bitmapThumbnail)
        } catch (e: Exception) {
            Log.d(MESSAGE_CENTER, "Error creating thumbnail. Using generic document icon.")
            val attachmentMimeType = findViewById<MaterialTextView>(R.id.apptentive_attachment_mime_text)
            attachmentMimeType.text = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).orEmpty()
        }

        val fileLayout = findViewById<ConstraintLayout>(R.id.apptentive_attachment_item)
        fileLayout.setOnClickListener { onClickFile.invoke() }
    }
}
