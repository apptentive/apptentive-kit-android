package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.payload.MediaType

internal class JSONPayloadPart(val json: String, private val containerKey: String?) : PayloadPart {
    override val contentType = MediaType.applicationJson
    override val filename = null
    override val parameterName = containerKey
    override val content = json.toByteArray()
}