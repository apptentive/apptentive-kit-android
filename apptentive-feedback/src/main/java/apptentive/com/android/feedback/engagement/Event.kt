package apptentive.com.android.feedback.engagement

data class Event(val vendor: String, val interaction: String, val name: String) {
    val fullName: String =
        "${escapeCharacters(vendor)}#${escapeCharacters(interaction)}#${escapeCharacters(name)}"

    override fun toString(): String = fullName

    companion object {
        fun local(name: String): Event = Event("local", "app", name)
        fun internal(name: String): Event = Event("com.apptentive", "app", name)

        private fun escapeCharacters(value: String): String = value
            .replace("%", "%25")
            .replace("#", "%23")
            .replace("/", "%2F")
    }
}