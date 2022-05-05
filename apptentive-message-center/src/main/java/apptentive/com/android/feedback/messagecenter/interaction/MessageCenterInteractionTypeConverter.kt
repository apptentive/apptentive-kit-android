package apptentive.com.android.feedback.messagecenter.interaction

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.AutomatedMessage
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.Composer
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.ErrorMessage
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.Greeting
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.Profile
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.Profile.Edit
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.Profile.Initial
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction.Status
import apptentive.com.android.util.optBoolean
import apptentive.com.android.util.optMap
import apptentive.com.android.util.optString

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
@Keep
internal class MessageCenterInteractionTypeConverter : InteractionTypeConverter<MessageCenterInteraction> {
    override fun convert(data: InteractionData): MessageCenterInteraction {
        val configuration = data.configuration
        return MessageCenterInteraction(
            messageCenterId = data.id,
            title = configuration.optString("title"),
            branding = configuration.optString("branding"),
            composer = configuration.optMap("composer")?.toComposer(),
            greeting = configuration.optMap("greeting")?.toGreeting(),
            status = configuration.optMap("status")?.toStatus(),
            automatedMessage = configuration.optMap("automated_message")?.toAutomatedMessage(),
            errorMessage = configuration.optMap("error_messages")?.toErrorMessage(),
            profile = configuration.optMap("profile")?.toProfile()
        )
    }

    private fun Map<String, Any?>.toComposer(): Composer =
        Composer(
            title = optString("title"),
            hintText = optString("hint_text"),
            sendButton = optString("send_button"),
            sendStart = optString("send_start"),
            sendOk = optString("send_ok"),
            sendFail = optString("send_fail"),
            closeText = optString("close_text"),
            closeBody = optString("close_confirm_body"),
            closeDiscard = optString("close_discard_button"),
            closeCancel = optString("close_cancel_button"),
        )

    private fun Map<String, Any?>.toGreeting(): Greeting =
        Greeting(
            title = optString("title"),
            body = optString("body"),
            image = optString("image_url"),
        )

    private fun Map<String, Any?>.toErrorMessage(): ErrorMessage =
        ErrorMessage(
            httpErrorMessage = optString("http_error_body"),
            networkErrorMessage = optString("network_error_body"),
        )

    private fun Map<String, Any?>.toStatus(): Status =
        Status(body = optString("body"))

    private fun Map<String, Any?>.toAutomatedMessage(): AutomatedMessage =
        AutomatedMessage(body = optString("body"))

    private fun Map<String, Any?>.toProfile(): Profile =
        Profile(
            request = optBoolean("request"),
            require = optBoolean("require"),
            initial = optMap("initial")?.toProfileInitial(),
            edit = optMap("edit")?.toProfileEdit()
        )
    private fun Map<String, Any?>.toProfileInitial() =
        Initial(
            title = optString("title"),
            nameHint = optString("name_hint"),
            emailHint = optString("email_hint"),
            skipButton = optString("skip_button"),
            saveButton = optString("save_button"),
            emailExplanation = optString("email_explanation"),
        )

    private fun Map<String, Any?>.toProfileEdit() =
        Edit(
            title = optString("title"),
            nameHint = optString("name_hint"),
            emailHint = optString("email_hint"),
            skipButton = optString("skip_button"),
            saveButton = optString("save_button"),
            emailExplanation = optString("email_explanation"),
        )
}
