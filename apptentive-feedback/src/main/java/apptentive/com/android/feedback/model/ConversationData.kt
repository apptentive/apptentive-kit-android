package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeNullableString

data class ConversationData(
    val localIdentifier: String,
    val conversationToken: String?,
    val conversationId: String?,
    val device: Device,
    val person: Person
)

internal fun Encoder.encode(obj: ConversationData) {
    encodeString(obj.localIdentifier)
    encodeNullableString(obj.conversationToken)
    encodeNullableString(obj.conversationId)
    encode(obj.device)
    encode(obj.person)
}

internal fun Decoder.decodeConversationData(): ConversationData {
    return ConversationData(
        localIdentifier = decodeString(),
        conversationToken = decodeNullableString(),
        conversationId = decodeNullableString(),
        device = decodeDevice(),
        person = decodePerson()
    )
}