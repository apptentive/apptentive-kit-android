package apptentive.com.android.feedback.messagecenter.view

import android.content.Intent
import android.graphics.Bitmap
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH
import apptentive.com.android.feedback.messagecenter.view.custom.MessageCenterAttachmentThumbnailView
import apptentive.com.android.feedback.messagecenter.view.custom.ProfileView
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.utils.convertToDate
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import com.google.android.material.textview.MaterialTextView

internal class MessageListAdapter(private val messageViewModel: MessageCenterViewModel) :
    ListAdapter<MessageViewData, RecyclerView.ViewHolder>(DiffCallback()) {

    private var profileView: ProfileView? = null
    private var restoreEmailFromDraft: String = ""
    private var restoreNameFromDraft: String = ""

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
        email?.let {
            if (profileView != null)
                profileView?.updateEmail(email)
            else
                restoreEmailFromDraft = email
        }
    }

    fun updateName(name: String?) {
        name?.let {
            if (profileView != null)
                profileView?.updateName(name)
            else
                restoreNameFromDraft = name
        }
    }

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
                val message = getItem(position).message
                with(holder.itemView) {
                    val groupTimestamp = findViewById<MaterialTextView>(R.id.apptentive_message_group_time_stamp)

                    // Inbound aka Message from US to the DASHBOARD (blue bubble)
                    val inboundLayout = findViewById<ConstraintLayout>(R.id.apptentive_message_inbound)
                    val inboundText = findViewById<MaterialTextView>(R.id.apptentive_message_inbound_text)
                    val inboundAttachments = findViewById<LinearLayout>(R.id.apptentive_message_inbound_attachments_layout)
                    val inboundStatus = findViewById<MaterialTextView>(R.id.apptentive_message_inbound_time_stamp)
                    val inboundError = findViewById<MaterialTextView>(R.id.apptentive_message_inbound_send_error)

                    // Outbound aka Message from the DASHBOARD to US (gray bubble)
                    val outboundLayout = findViewById<ConstraintLayout>(R.id.apptentive_message_outbound)
                    val outboundText = findViewById<MaterialTextView>(R.id.apptentive_message_outbound_text)
                    val outboundAttachments = findViewById<LinearLayout>(R.id.apptentive_message_outbound_attachments_layout)
                    val outboundStatus = findViewById<MaterialTextView>(R.id.apptentive_message_outbound_time_stamp)
                    val outboundSenderText = findViewById<MaterialTextView>(R.id.apptentive_message_outbound_sender_text)

                    groupTimestamp.visibility = when {
                        // valid time stamp & a view only with an un-responded automated message
                        message?.groupTimestamp != null && itemCount == 3 &&
                            message.automated == true && message.messageStatus == Message.Status.Sending ->
                            View.INVISIBLE
                        // null time stamp
                        message?.groupTimestamp == null -> View.GONE
                        // valid time stamp
                        else -> View.VISIBLE
                    }
                    groupTimestamp.text = message?.groupTimestamp

                    if (message?.inbound == true) { // Message from SDK to the DASHBOARD (blue bubble)
                        inboundLayout.isVisible = true
                        outboundLayout.isVisible = false
                        inboundText.text = message.body
                        inboundText.isVisible = !message.body.isNullOrEmpty()
                        inboundText.text = message.body
                        try {
                            Linkify.addLinks(inboundText, Linkify.ALL)
                            inboundText.movementMethod = LinkMovementMethod.getInstance()
                        } catch (exception: Exception) {
                            Log.e(MESSAGE_CENTER, "Couldn't add linkify to inbound text", exception)
                        }

                        inboundAttachments.removeAllViews()
                        inboundAttachments.addAttachments(message)
                        val status =
                            if (message.messageStatus == Message.Status.Saved) Message.Status.Sent
                            else message.messageStatus
                        inboundStatus.text = "$status \u2022 ${convertToDate(message.createdAt)}"
                        inboundError.isVisible = message.messageStatus == Message.Status.Failed
                    } else { // Message from the DASHBOARD to SDK (gray bubble)
                        inboundLayout.isVisible = false
                        inboundError.isVisible = false
                        outboundLayout.isVisible = true
                        outboundText.text = message?.body
                        try {
                            Linkify.addLinks(outboundText, Linkify.ALL)
                            outboundText.movementMethod = LinkMovementMethod.getInstance()
                        } catch (exception: Exception) {
                            Log.e(MESSAGE_CENTER, "Couldn't add linkify to outbound text", exception)
                        }
                        outboundText.isVisible = !message?.body.isNullOrEmpty()
                        if (message?.sender?.name.isNullOrEmpty()) outboundSenderText.visibility = View.GONE
                        else outboundSenderText.text = message?.sender?.name
                        if (message?.createdAt != null) outboundStatus.text = convertToDate(message.createdAt)
                        outboundAttachments.removeAllViews()
                        outboundAttachments.addAttachments(message)
                    }
                }
            }

            is MessageHeaderViewHolder -> {
                val greetingData = getItem(position).greetingData
                val greetingTitle = holder.itemView.findViewById<TextView>(R.id.apptentive_message_center_greeting_title)
                val greetingBodyTextView = holder.itemView.findViewById<TextView>(R.id.apptentive_message_center_greeting_body)
                holder.itemView.findViewById<ImageView>(R.id.apptentive_message_center_greeting_image).apply {
                    greetingData?.avatarBitmap?.let { avatar ->
                        this.setImageBitmap(avatar)
                    }
                }
                // Assign the text value before adding the links to make sure they are rendered as links
                greetingTitle.text = greetingData?.greetingTitle
                greetingBodyTextView.text = greetingData?.greetingBody
                try {
                    Linkify.addLinks(greetingTitle, Linkify.ALL)
                    Linkify.addLinks(greetingBodyTextView, Linkify.ALL)
                    greetingBodyTextView.movementMethod = LinkMovementMethod.getInstance()
                } catch (exception: Exception) {
                    Log.e(MESSAGE_CENTER, "Couldn't add linkify to greeting text", exception)
                }
            }

            is MessageFooterViewHolder -> {
                profileView =
                    holder.itemView.findViewById(R.id.apptentive_message_center_profile)
                val statusView: MaterialTextView =
                    holder.itemView.findViewById(R.id.apptentive_message_center_status)
                statusView.text = messageViewModel.messageSLA
                statusView.movementMethod = LinkMovementMethod.getInstance()

                val profileData = getItem(position).profileData

                profileData?.let { viewData ->
                    profileView?.setEmailHint(viewData.emailHint)
                    profileView?.setNameHint(viewData.nameHint)
                    profileView?.isVisible = viewData.showProfile
                    profileView?.updateName(restoreNameFromDraft)
                    profileView?.updateEmail(restoreEmailFromDraft)
                }
            }
        }
    }

    private fun LinearLayout.addAttachments(message: Message?) {
        message?.attachments?.forEach { file ->
            addView(
                MessageCenterAttachmentThumbnailView(context, null).apply {
                    setAttachmentView(file) {
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
            currentList.size - 1 -> TYPE_FOOTER
            else -> TYPE_ITEMS
        }
    }

    override fun getItemCount(): Int = currentList.size

    private class DiffCallback : DiffUtil.ItemCallback<MessageViewData>() {
        override fun areItemsTheSame(oldItem: MessageViewData, newItem: MessageViewData) =
            oldItem.listItemType == newItem.listItemType && oldItem.message?.id == newItem.message?.id

        override fun areContentsTheSame(oldItem: MessageViewData, newItem: MessageViewData): Boolean =
            oldItem == newItem
    }
}

class MessageHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class MessageFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
data class GreetingData(val greetingTitle: String, val greetingBody: String, val avatarBitmap: Bitmap?)
data class ProfileViewData(val emailHint: String, val nameHint: String, val showProfile: Boolean)
data class MessageViewData(
    val listItemType: ListItemType,
    val greetingData: GreetingData?,
    val profileData: ProfileViewData?,
    val message: Message?
)
enum class ListItemType {
    HEADER, FOOTER, MESSAGE
}
