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

private const val SKIPPED_QUESTION = "skipped"
private const val EMPTY_QUESTION = "empty"
private const val ANSWERED_QUESTION = "answered"
@Keep
internal class SurveyResponsePayload(
    nonce: String = generateUUID(),
    val id: String,
    val answers: Map<String, AnswerStateData>
) : ConversationPayload(nonce) {

    data class AnswerStateData(
        val state: String,
        val value: List<AnswerData>? = null
    )
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
            answers: Map<String, SurveyAnswerState>
        ) = SurveyResponsePayload(
            id = id,
            answers = buildAnswerStateData(answers)
        )

        private fun buildAnswerStateData(answers: Map<String, SurveyAnswerState>) =
            answers.map { (id, answers) ->
                id to when (answers) {
                    is SurveyAnswerState.Answered -> AnswerStateData(
                        state = ANSWERED_QUESTION,
                        value = convertAnswer(answers)
                    )
                    is SurveyAnswerState.Empty -> AnswerStateData(state = EMPTY_QUESTION)
                    is SurveyAnswerState.Skipped -> AnswerStateData(state = SKIPPED_QUESTION)
                }
            }.toMap()

        private fun convertAnswer(answer: SurveyAnswerState.Answered) =
            when (answer.answer) {
                is SingleLineQuestion.Answer -> listOf(AnswerData(value = answer.answer.value))
                is RangeQuestion.Answer -> listOf(AnswerData(value = answer.answer.selectedIndex))
                is MultiChoiceQuestion.Answer -> answer.answer.choices.mapNotNull { if (it.checked) AnswerData(it.id, it.value) else null }
                else -> throw IllegalArgumentException("Unexpected type: ${answer::class.java}")
            }
    }
}
