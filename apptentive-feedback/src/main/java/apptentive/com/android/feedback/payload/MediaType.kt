package apptentive.com.android.feedback.payload

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class MediaType(
    val type: String,
    val subType: String,
    val boundary: String? = null
) {
    override fun toString(): String {
        return if (boundary == null) "$type/$subType" else "$type/$subType;boundary=$boundary"
    }

    companion object {
        fun parse(value: String): MediaType {
            val tokens = value.split("/")
            if (tokens.size !in 2..3) {
                throw IllegalArgumentException("Invalid value for media type: $value")
            }

            return MediaType(
                type = tokens[0],
                subType = tokens[1],
                boundary = tokens.getOrNull(2)
            )
        }

        val applicationJson = MediaType("application", "json")
        val applicationOctetStream = MediaType("application", "octet-stream")
        fun multipartUnauthenticated(boundary: String) = MediaType("multipart", "mixed", boundary)
    }
}
