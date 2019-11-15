package apptentive.com.android.feedback.payload

data class MediaType(
    val type: String,
    val subType: String
) {
    override fun toString(): String {
        return "$type/$subType"
    }

    companion object {
        fun parse(value: String) {
            TODO()
        }

        val applicationJson = MediaType("application", "json")
        val applicationOctetStream = MediaType("application", "octet-stream")
    }
}
