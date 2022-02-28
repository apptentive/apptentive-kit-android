package apptentive.com.android.feedback.model.payloads

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter

abstract class Payload(val nonce: String) {
    protected abstract fun getPayloadType(): PayloadType
    protected abstract fun getJsonContainer(): String?
    protected abstract fun getHttpMethod(): HttpMethod
    protected abstract fun getHttpPath(): String
    protected abstract fun getContentType(): MediaType

    fun toJson(): String {
        val container = getJsonContainer()
        if (container != null) {
            return JsonConverter.toJson(mapOf(container to this))
        }
        return JsonConverter.toJson(this)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun toPayloadData() = PayloadData(
        nonce = nonce,
        type = getPayloadType(),
        path = getHttpPath(),
        method = getHttpMethod(),
        mediaType = getContentType(),
        data = toJson().toByteArray()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Payload) return false

        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        return nonce.hashCode()
    }
}
