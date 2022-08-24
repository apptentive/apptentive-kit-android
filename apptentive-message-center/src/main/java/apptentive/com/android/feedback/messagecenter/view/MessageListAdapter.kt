package apptentive.com.android.feedback.messagecenter.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.utils.convertToDate
import com.google.android.material.textview.MaterialTextView

class MessageListAdapter(dataSet: List<Message>) : RecyclerView.Adapter<MessageViewHolder>() {

    val listItems: MutableList<Message> = dataSet.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.apptentive_item_message_bubble, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = listItems[position]
        with(holder.itemView) {
            val groupTimestamp = findViewById<MaterialTextView>(R.id.apptentive_message_group_time_stamp)
            val inboundLayout = findViewById<ConstraintLayout>(R.id.apptentive_message_inbound)
            val outboundLayout = findViewById<ConstraintLayout>(R.id.apptentive_message_outbound)
            val inboundText = findViewById<MaterialTextView>(R.id.apptentive_message_inbound_text)
            val inboundAttachments = findViewById<LinearLayout>(R.id.apptentive_message_inbound_attachments_layout)
            val inboundStatus = findViewById<MaterialTextView>(R.id.apptentive_message_inbound_time_stamp)
            val outboundText = findViewById<MaterialTextView>(R.id.apptentive_message_outbound_text)
            val outboundAttachments = findViewById<LinearLayout>(R.id.apptentive_message_outbound_attachments_layout)
            val outboundStatus = findViewById<MaterialTextView>(R.id.apptentive_message_outbound_time_stamp)

            groupTimestamp.isVisible = message.groupTimestamp != null
            groupTimestamp.text = message.groupTimestamp

            if (message.inbound) { // Message from US to the BACKEND
                inboundLayout.visibility = View.VISIBLE
                outboundLayout.visibility = View.GONE
                inboundText.isVisible = !message.body.isNullOrEmpty()
                inboundText.text = message.body
                inboundAttachments.removeAllViews()
                message.storedFiles.forEach { file ->
                    inboundAttachments.addView(
                        MessageCenterAttachmentThumbnailView(context, null).apply {
                            setAttachmentView(file.localFilePath, file.mimeType) { }
                        }
                    )
                }
                val status = if (message.messageStatus == Message.Status.Saved) Message.Status.Sent else message.messageStatus
                inboundStatus.text = "$status \u2022 ${convertToDate(message.createdAt)}"
            } else { // Message from the BACKEND to US
                inboundLayout.visibility = View.GONE
                outboundLayout.visibility = View.VISIBLE
                outboundText.text = message.body
                outboundText.isVisible = !message.body.isNullOrEmpty()
                outboundStatus.text = convertToDate(message.createdAt)
                outboundAttachments.removeAllViews()
                message.storedFiles.orEmpty().forEach { file ->
                    outboundAttachments.addView(
                        MessageCenterAttachmentThumbnailView(context, null).apply {
                            setAttachmentView(file.localFilePath, file.mimeType) { }
                        }
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int = listItems.size
}

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
