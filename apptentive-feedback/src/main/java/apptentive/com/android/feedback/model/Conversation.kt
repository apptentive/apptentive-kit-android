package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeNullableString

data class Conversation(
    val localIdentifier: String,
    val conversationToken: String? = null,
    val conversationId: String? = null,
    val device: Device,
    val person: Person,
    val sdk: SDK,
    val appRelease: AppRelease,
    val engagementManifest: EngagementManifest
)

val Conversation.hasConversationToken get() = this.conversationToken != null

internal fun Encoder.encodeConversation(obj: Conversation) {
    encodeString(obj.localIdentifier)
    encodeNullableString(obj.conversationToken)
    encodeNullableString(obj.conversationId)
    encodeDevice(obj.device)
    encodePerson(obj.person)
    encodeSDK(obj.sdk)
    encodeAppRelease(obj.appRelease)
    encodeEngagementManifest(obj.engagementManifest)
}

internal fun Decoder.decodeConversation(): Conversation {
    return Conversation(
        localIdentifier = decodeString(),
        conversationToken = decodeNullableString(),
        conversationId = decodeNullableString(),
        device = decodeDevice(),
        person = decodePerson(),
        sdk = decodeSDK(),
        appRelease = decodeAppRelease(),
        engagementManifest = decodeEngagementManifest()
    )
}