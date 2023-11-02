package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.payload.MediaType

internal class AttachmentPayloadPart(override val content: ByteArray, override val contentType: MediaType, override val filename: String?) :
    PayloadPart {
    override val parameterName get() = "file[]"
}
