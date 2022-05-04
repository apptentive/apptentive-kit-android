package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.util.InternalUseOnly

/**
 * @param vendor the organization that created the event. For start, there will be two defined.
 * @param interaction the name of the interaction that the event was invoked through. In the case of events lying outside of an interaction, use <code>app</code>.
 * @param name the actual name of the event.
 */
@InternalUseOnly
data class Event(val vendor: String, val interaction: String, val name: String) {
    val fullName: String =
        "${escapeCharacters(vendor)}#${escapeCharacters(interaction)}#${escapeCharacters(name)}"

    override fun toString(): String = fullName

    companion object {
        fun local(name: String) = Event("local", "app", name)
        fun internal(name: String, interaction: String = "app") = Event("com.apptentive", interaction, name)
        fun internal(name: String, interaction: InteractionType) = internal(name, interaction.toString())

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

enum class InternalEvent(val labelName: String) {
    EVENT_REQUEST("request"),
    EVENT_SHOWN("shown"),
    EVENT_NOT_SHOWN("not_shown"),
    EVENT_NOT_SUPPORTED("not_supported"),
    EVENT_LAUNCH("launch"),
    APP_LAUNCH("launch"),
    APP_EXIT("exit")
}
