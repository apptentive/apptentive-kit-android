package apptentive.com.android.feedback.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
data class ConversationData(
    val localIdentifier: String,
    val conversationToken: String? = null,
    val conversationId: String? = null
) {

    //region kotlinx custom serialization

    @Serializer(forClass = ConversationData::class)
    companion object : KSerializer<ConversationData> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName("ConversationData")

        override fun deserialize(input: Decoder): ConversationData {
            val localIdentifier = input.decodeString()
            val conversationToken = input.decodeNullableString()
            val conversationId = input.decodeNullableString()
            return ConversationData(localIdentifier, conversationToken, conversationId)
        }

        override fun serialize(output: Encoder, obj: ConversationData) {
            output.encodeString(obj.localIdentifier)
            output.encodeNullableString(obj.conversationToken)
            output.encodeNullableString(obj.conversationId)
        }
    }

    //endregion
}