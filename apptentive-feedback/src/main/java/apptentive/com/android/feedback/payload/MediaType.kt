package apptentive.com.android.feedback.payload

data class MediaType(
    val type: String,
    val subType: String
) {
    override fun toString(): String {
        return "$type/$subType"
    }

    companion object {
        fun parse(value: String): MediaType {
            val tokens = value.split("/")
            if (tokens.size != 2) {
                throw IllegalArgumentException("Invalid value for media type: $value")
            }

            return MediaType(
                type = tokens[0],
                subType = tokens[1]
            )
        }

        val applicationJson = MediaType("application", "json")
        val applicationOctetStream = MediaType("application", "octet-stream")
    }
}
