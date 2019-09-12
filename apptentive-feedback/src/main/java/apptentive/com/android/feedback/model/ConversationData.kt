package apptentive.com.android.feedback.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
data class ConversationData(
    val localIdentifier: String,
    val conversationToken: String?,
    val conversationId: String?,
    val device: Device
) {

    //region kotlinx custom serialization

    @UseExperimental(ImplicitReflectionSerializer::class)
    @Serializer(forClass = ConversationData::class)
    companion object : KSerializer<ConversationData> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName("ConversationData")

        override fun deserialize(input: Decoder): ConversationData {
            return ConversationData(
                localIdentifier = input.decodeString(),
                conversationToken = input.decodeNullableString(),
                conversationId = input.decodeNullableString(),
                device = input.decode()
            )
        }

        override fun serialize(output: Encoder, obj: ConversationData) {
            output.encodeString(obj.localIdentifier)
            output.encodeNullableString(obj.conversationToken)
            output.encodeNullableString(obj.conversationId)
            output.encode(obj.device)
        }
    }

    //endregion
}