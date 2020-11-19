package apptentive.com.android.feedback.engagement

/**
 * @param vendor the organization that created the event. For start, there will be two defined.
 * @param interaction the name of the interaction that the event was invoked through. In the case of events lying outside of an interaction, use <code>app</code>.
 * @param name the actual name of the event.
 */
data class Event(val vendor: String, val interaction: String, val name: String) {
    val fullName: String =
        "${escapeCharacters(vendor)}#${escapeCharacters(interaction)}#${escapeCharacters(name)}"

    override fun toString(): String = fullName

    companion object {
        fun local(name: String): Event = Event("local", "app", name)
        fun internal(name: String, interaction: String = "app"): Event = Event("com.apptentive", interaction, name)

        private fun escapeCharacters(value: String): String = value
            .replace("%", "%25")
            .replace("#", "%23")
            .replace("/", "%2F")

        fun parse(value: String): Event {
            val tokens = value.split("#")
            require(tokens.size == 3) { "Invalid event name: '$value'" }
            return Event(vendor = tokens[0], interaction = tokens[1], name = tokens[2])
        }
    }
}