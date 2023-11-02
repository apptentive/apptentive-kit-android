package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.payload.MediaType

internal class JSONPayloadPart(val json: String, private val containerKey: String?) : PayloadPart {
    override val contentType get() = MediaType.applicationJson
    override val filename get() = null
    override val parameterName get() = containerKey
    override val content get() = json.toByteArray()
}