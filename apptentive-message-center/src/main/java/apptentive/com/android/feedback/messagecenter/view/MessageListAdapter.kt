package apptentive.com.android.feedback.messagecenter.view

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH
import apptentive.com.android.feedback.messagecenter.view.custom.MessageCenterAttachmentThumbnailView
import apptentive.com.android.feedback.messagecenter.view.custom.ProfileView
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.utils.convertToDate
import com.google.android.material.textview.MaterialTextView

class MessageListAdapter(
    dataSet: List<MessageViewData>,
    private val messageViewModel: MessageCenterViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val listItems: MutableList<MessageViewData> = dataSet.toMutableList()

    private var profileView: ProfileView? = null

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEMS = 1
        private const val TYPE_FOOTER = 2
    }

    fun getProfileName(): String = profileView?.getName().orEmpty()

    fun getProfileEmail(): String = profileView?.getEmail().orEmpty().trim()

    fun setEmailError(value: Boolean) {
        profileView?.setEmailError(value)
    }

    fun setNameError(value: Boolean) {
        profileView?.setNameError(value)
    }

    fun updateEmail(email: String?) {
        email?.let { profileView?.updateEmail(email) }
    }

    fun updateName(name: String?) {
        name?.let { profileView?.updateName(name) }
    }

    fun isProfileViewVisible(): Boolean = profileView?.isVisible == true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view =
                    layoutInflater.inflate(R.layout.apptentive_item_message_header, parent, false)
                MessageHeaderViewHolder(view)
            }

            TYPE_ITEMS -> {
                val view = layoutInflater
                    .inflate(R.layout.apptentive_item_message_bubble, parent, false)
                MessageViewHolder(view)
            }

            TYPE_FOOTER -> {
                val view =
                    layoutInflater.inflate(R.layout.apptentive_item_message_footer, parent, false)
                MessageFooterViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MessageViewHolder -> {
                val message = listItems[position].message
                with(holder.itemView) {
                    val groupTimestamp =
                        findViewById<MaterialTextView>(R.id.apptentive_message_group_time_stamp)
                    val inboundLayout =
                        findViewById<ConstraintLayout>(R.id.apptentive_message_inbound)
                    val outboundLayout =
                        findViewById<ConstraintLayout>(R.id.apptentive_message_outbound)
                    val inboundText =
                        findViewById<MaterialTextView>(R.id.apptentive_message_inbound_text)
                    val inboundAttachments =
                        findViewById<LinearLayout>(R.id.apptentive_message_inbound_attachments_layout)
                    val inboundStatus =
                        findViewById<MaterialTextView>(R.id.apptentive_message_inbound_time_stamp)
                    val outboundText =
                        findViewById<MaterialTextView>(R.id.apptentive_message_outbound_text)
                    val outboundAttachments =
                        findViewById<LinearLayout>(R.id.apptentive_message_outbound_attachments_layout)
                    val outboundStatus =
                        findViewById<MaterialTextView>(R.id.apptentive_message_outbound_time_stamp)
                    val outboundSenderText =
                        findViewById<MaterialTextView>(R.id.apptentive_message_outbound_sender_text)

                    groupTimestamp.visibility = when {
                        // valid time stamp & a view only with an unresponded automated message
                        message?.groupTimestamp != null && itemCount == 3 &&
                            message.automated == true && message.messageStatus == Message.Status.Sending ->
                            View.INVISIBLE
                        // null time stamp
                        message?.groupTimestamp == null -> View.GONE
                        // valid time stamp
                        else -> View.VISIBLE
                    }
                    groupTimestamp.text = message?.groupTimestamp

                    if (message?.inbound == true) { // Message from US to the BACKEND
                        inboundLayout.visibility = View.VISIBLE
                        outboundLayout.visibility = View.GONE
                        inboundText.text = message.body
                        inboundText.isVisible = !message.body.isNullOrEmpty()
                        inboundText.text = message.body
                        inboundAttachments.removeAllViews()
                        inboundAttachments.addAttachments(message)
                        val status =
                            if (message.messageStatus == Message.Status.Saved) Message.Status.Sent else message.messageStatus
                        inboundStatus.text = "$status \u2022 ${convertToDate(message.createdAt)}"
                    } else { // Message from the BACKEND to US
                        inboundLayout.visibility = View.GONE
                        outboundLayout.visibility = View.VISIBLE
                        outboundText.text = message?.body
                        outboundText.isVisible = !message?.body.isNullOrEmpty()
                        if (message?.sender?.name.isNullOrEmpty())
                            outboundSenderText.visibility = View.GONE
                        else
                            outboundSenderText.text = message?.sender?.name
                        outboundStatus.text = convertToDate(message?.createdAt ?: 0.0)
                        outboundAttachments.removeAllViews()
                        outboundAttachments.addAttachments(message)
                    }
                }
            }

            is MessageHeaderViewHolder -> {
                val greetingData = listItems[position].greetingData
                holder.itemView.findViewById<TextView>(R.id.apptentive_message_center_greeting).text =
                    greetingData?.greetingTitle
                holder.itemView.findViewById<TextView>(R.id.apptentive_message_center_greeting_body).text =
                    greetingData?.greetingBody
                holder.itemView.findViewById<ImageView>(R.id.apptentive_message_center_greeting_header).apply {
                    greetingData?.avatarBitmap?.let { avatar ->
                        this.setImageBitmap(avatar)
                    }
                }
            }

            is MessageFooterViewHolder -> {
                profileView =
                    holder.itemView.findViewById(R.id.apptentive_message_center_profile)
                val statusView: MaterialTextView =
                    holder.itemView.findViewById(R.id.apptentive_message_center_status)
                statusView.text = messageViewModel.messageSLA

                if (messageViewModel.showLauncherView || messageViewModel.showProfile() && listItems.size > 2 && listItems[1].message?.automated == true && listItems[1].message?.messageStatus == Message.Status.Sending) {
                    val profileData = listItems[position].profileData
                    profileView?.setEmailHint(profileData?.emailHint ?: "")
                    profileView?.setNameHint(profileData?.nameHint ?: "")
                } else {
                    profileView?.visibility = View.GONE
                }
            }
        }
    }

    private fun LinearLayout.addAttachments(message: Message?) {
        message?.attachments?.forEach { file ->
            addView(
                MessageCenterAttachmentThumbnailView(context, null).apply {
                    setAttachmentView(file, messageViewModel.isFileDownloading(file)) {
                        if (file.hasLocalFile()) {
                            context.startActivity(
                                Intent(context, ImagePreviewActivity::class.java).apply {
                                    putExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME, file.originalName)
                                    putExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH, file.localFilePath)
                                }
                            )
                        } else messageViewModel.downloadFile(message, file)
                    }
                }
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            listItems.size - 1 -> TYPE_FOOTER
            else -> TYPE_ITEMS
        }
    }

    override fun getItemCount(): Int = listItems.size
}

class MessageHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class MessageFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
data class GreetingData(val greetingTitle: String, val greetingBody: String, val avatarBitmap: Bitmap?)
data class ProfileViewData(val emailHint: String, val nameHint: String, val visibility: Boolean)
data class MessageViewData(
    val greetingData: GreetingData?,
    val profileData: ProfileViewData?,
    val message: Message?
)
