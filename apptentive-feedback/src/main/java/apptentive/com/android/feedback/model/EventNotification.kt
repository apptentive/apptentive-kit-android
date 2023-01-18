package apptentive.com.android.feedback.model

/**
 * Data class for sharing the event stream of the Apptentive SDK
 *
 * @param name the name of the event that was passed into the `engage` method.
 * @param vendor the organization that created the event. "com.apptentive" if SDK engaged or "local" if app engaged.
 * @param interaction the name of the interaction that the event was engaged from. "app" if not engaged from an interaction.
 * @param interactionId the id of the interaction that the event was engaged from. `null` if not engaged from an interaction.
 */
data class EventNotification(
    val name: String,
    val vendor: String,
    val interaction: String,
    val interactionId: String?
)
