package apptentive.com.android.feedback.messagecenter.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.utils.convertToDate

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
            val inboundLayout = findViewById<ConstraintLayout>(R.id.apptentive_message_inbound)
            val outboundLayout = findViewById<ConstraintLayout>(R.id.apptentive_message_outbound)
            val inboundText = findViewById<TextView>(R.id.apptentive_message_inbound_text)
            val inboundStatus = findViewById<TextView>(R.id.apptentive_message_inbound_time_stamp)
            val outboundText = findViewById<TextView>(R.id.apptentive_message_outbound_text)
            val outboundStatus = findViewById<TextView>(R.id.apptentive_message_outbound_time_stamp)
            if (message.inbound) {
                inboundLayout.visibility = View.VISIBLE
                outboundLayout.visibility = View.GONE
                inboundText.text = message.body
                // TODO don't want to introduce a string file temporarily. Message status should be fetched from manifest!
                inboundStatus.text = "Sent ${convertToDate(message.createdAt)}"
            } else {
                inboundLayout.visibility = View.GONE
                outboundLayout.visibility = View.VISIBLE
                outboundText.text = message.body
                outboundStatus.text = "Sent ${convertToDate(message.createdAt)}"
            }
        }
    }

    override fun getItemCount(): Int = listItems.size
}

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
