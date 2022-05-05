package apptentive.com.android.feedback.messagecenter.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal data class MessageCenterInteraction(
    val messageCenterId: String,
    val title: String?,
    val branding: String?,
    val composer: Composer?,
    val greeting: Greeting?,
    val status: Status?,
    val automatedMessage: AutomatedMessage?,
    val errorMessage: ErrorMessage?,
    val profile: Profile?,
) : Interaction(messageCenterId, type = InteractionType.MessageCenter) {

    data class Composer(
        val title: String?,
        val closeBody: String?,
        val closeDiscard: String?,
        val closeCancel: String?,
        val sendButton: String?,
        val hintText: String?
    )

    data class Greeting(
        val title: String?,
        val body: String?,
        val image: String?
    )

    data class ErrorMessage(
        val httpErrorMessage: String?,
        val networkErrorMessage: String?
    )

    data class Status(val body: String?)

    data class AutomatedMessage(val body: String?)

    data class Profile(
        val request: Boolean?,
        val require: Boolean?,
        val initial: Initial?,
        val edit: Edit?
    ) {
        data class Initial(
            val title: String?,
            val nameHint: String?,
            val emailHint: String?,
            val skipButton: String?,
            val saveButton: String?,
            val emailExplanation: String?
        )
        data class Edit(
            val title: String?,
            val nameHint: String?,
            val emailHint: String?,
            val skipButton: String?,
            val saveButton: String?,
            val emailExplanation: String?
        )
    }
}
