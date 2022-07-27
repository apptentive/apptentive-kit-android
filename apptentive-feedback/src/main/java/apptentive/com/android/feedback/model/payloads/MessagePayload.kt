package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.generateUUID

/**
 * A payload class to send messages.
 *
 * @param messageNonce - The nonce assigned to the message
 * @param body - Body of the message
 * @param sender -  Data container class for Message Sender.
 * @param hidden - Flag to determine whether the message should be hidden in the Message Center UI
 * @param automated - Flag to determine whether the message was sent by an automatic process
 */

@InternalUseOnly
data class MessagePayload(
    val messageNonce: String = generateUUID(),
    val type: String,
    val body: String,
    val sender: Sender?,
    val hidden: Boolean = false,
    val automated: Boolean = false,
    val customData: Map<String, Any?>? = null
) : ConversationPayload(messageNonce) {
    override fun getPayloadType(): PayloadType = PayloadType.Message

    override fun getContentType(): MediaType = MediaType.applicationJson

    override fun getHttpMethod(): HttpMethod = HttpMethod.POST

    override fun getHttpPath(): String = Constants.buildHttpPath("messages")

    override fun getJsonContainer(): String = "message"
}
