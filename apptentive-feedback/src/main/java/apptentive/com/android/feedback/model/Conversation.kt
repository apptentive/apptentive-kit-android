package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.*

enum class ConversationState {
    /** Conversation state is not known */
    UNDEFINED,
    /** No logged in user and no conversation token */
    ANONYMOUS_PENDING,
    /** No logged in user with conversation token */
    ANONYMOUS,
    /** The activeConversation belongs to the currently logged-in user */
    LOGGED_IN,
    /** The activeConversation belongs to a logged-out user */
    LOGGED_OUT
}

data class Conversation(
    val localIdentifier: String,
    val conversationToken: String? = null,
    val conversationId: String? = null,
    val state: ConversationState = ConversationState.UNDEFINED,
    val device: Device,
    val person: Person
)

internal fun Encoder.encodeConversation(obj: Conversation) {
    encodeString(obj.localIdentifier)
    encodeNullableString(obj.conversationToken)
    encodeNullableString(obj.conversationId)
    encodeEnum(obj.state)
    encodeDevice(obj.device)
    encodePerson(obj.person)
}

internal fun Decoder.decodeConversation(): Conversation {
    return Conversation(
        localIdentifier = decodeString(),
        conversationToken = decodeNullableString(),
        conversationId = decodeNullableString(),
        state = decodeEnum(),
        device = decodeDevice(),
        person = decodePerson()
    )
}