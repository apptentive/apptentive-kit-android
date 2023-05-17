package apptentive.com.android.feedback.survey.model

import androidx.annotation.Keep
import apptentive.com.android.feedback.Constants.buildHttpPath
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.model.payloads.ConversationPayload
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.generateUUID

@Keep
internal class SurveyResponsePayload(
    nonce: String = generateUUID(),
    val id: String,
    val answers: Map<String, List<AnswerData>>
) : ConversationPayload(nonce) {
    data class AnswerData(
        val id: String? = null,
        val value: Any? = null
    )

    override fun getPayloadType() = PayloadType.SurveyResponse

    override fun getJsonContainer() = "response"

    override fun getHttpMethod() = HttpMethod.POST

    override fun getHttpPath() = buildHttpPath("surveys/$id/responses")

    override fun getContentType() = MediaType.applicationJson

    override fun getDataBytes() = toJson().toByteArray()

    override fun getAttachmentDataBytes() = AttachmentData()

    companion object {
        fun fromAnswers(
            id: InteractionId,
            answers: Map<String, SurveyQuestionAnswer>
        ) = SurveyResponsePayload(
            id = id,
            answers = answers
                .map { (id, answer) -> id to convertAnswer(answer) }
                .toMap()
        )

        private fun convertAnswer(answer: SurveyQuestionAnswer) =
            when (answer) {
                is SingleLineQuestion.Answer -> listOf(AnswerData(value = answer.value))
                is RangeQuestion.Answer -> listOf(AnswerData(value = answer.selectedIndex))
                is MultiChoiceQuestion.Answer -> answer.choices.mapNotNull { if (it.checked) AnswerData(it.id, it.value) else null }
                else -> throw IllegalArgumentException("Unexpected type: ${answer::class.java}")
            }
    }
}
