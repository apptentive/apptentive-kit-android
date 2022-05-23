package apptentive.com.android.feedback.messagecenter

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteractionTypeConverter
import apptentive.com.android.serialization.json.JsonConverter
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

class MessageCenterInteractionTypeConverterTest : TestCase() {
    @Test
    fun testConvert() {
        val json = """
            {
              "id": "message_center_interaction_id",
              "type": "MessageCenter",
              "version": 2,
              "configuration": {
                "title": "Message Center",
                "branding": "Powered by Apptentive",
                "composer": {
                  "title": "New Message",
                  "hint_text": "Please leave detailed feedback",
                  "send_button": "Send",
                  "send_start": "Sending...",
                  "send_ok": "Sent",
                  "send_fail": "Failed",
                  "close_text": "Close",
                  "close_confirm_body": "Are you sure you want to discard this message?",
                  "close_discard_button": "Discard",
                  "close_cancel_button": "Cancel"
                },
                "greeting": {
                  "title": "Hello!",
                  "body": "We'd love to get feedback from you on our app. The more details you can provide, the better.",
                  "image_url": "https://apptentive-app-icons-production.s3.amazonaws.com/521f9bba68e2758b6d00098d_1443569347.png"
                },
                "status": {
                  "body": "We will respond to your message soon."
                },
                "automated_message": {
                  "body": "Please let us know how to make Temp Dev App better for you!"
                },
                "error_messages": {
                  "http_error_body": "It looks like we're having trouble sending your message. We've saved it and will try sending it again soon.",
                  "network_error_body": "It looks like you aren't connected to the Internet right now. We've saved your message and will try again when we detect a connection."
                },
                "profile": {
                  "request": false,
                  "require": false,
                  "initial": {
                    "title": "Initial Title",
                    "name_hint": "Name",
                    "email_hint": "Email",
                    "skip_button": "Skip",
                    "save_button": "That's Me!",
                    "email_explanation": "Email explanation"
                  },
                  "edit": {
                    "title": "Edit Title",
                    "name_hint": "Name",
                    "email_hint": "Email",
                    "skip_button": "Cancel",
                    "save_button": "Save",
                    "email_explanation": "Email explanation"
                  }
                }
              }
            }
        """.trimIndent()

        val data = JsonConverter.fromJson<InteractionData>(json.trimIndent())
        val actual = MessageCenterInteractionTypeConverter().convert(data)
        val expected = MessageCenterInteraction(
            messageCenterId = "message_center_interaction_id",
            title = "Message Center",
            branding = "Powered by Apptentive",
            composer = MessageCenterInteraction.Composer(
                "New Message",
                "Please leave detailed feedback",
                "Send",
                "Sending...",
                "Sent",
                "Failed",
                "Close",
                "Are you sure you want to discard this message?",
                "Discard",
                "Cancel"
            ),
            greeting = MessageCenterInteraction.Greeting(
                "Hello!",
                "We'd love to get feedback from you on our app. The more details you can provide, the better.",
                "https://apptentive-app-icons-production.s3.amazonaws.com/521f9bba68e2758b6d00098d_1443569347.png"
            ),
            status = MessageCenterInteraction.Status(
                "We will respond to your message soon."
            ),
            automatedMessage = MessageCenterInteraction.AutomatedMessage(
                "Please let us know how to make Temp Dev App better for you!"
            ),
            errorMessage = MessageCenterInteraction.ErrorMessage(
                "It looks like we're having trouble sending your message. We've saved it and will try sending it again soon.",
                "It looks like you aren't connected to the Internet right now. We've saved your message and will try again when we detect a connection."
            ),
            profile = MessageCenterInteraction.Profile(
                request = false,
                require = false,
                initial = MessageCenterInteraction.Profile.Initial(
                    "Initial Title",
                    "Name",
                    "Email",
                    "Skip",
                    "That's Me!",
                    "Email explanation"
                ),
                edit = MessageCenterInteraction.Profile.Edit(
                    "Edit Title",
                    "Name",
                    "Email",
                    "Cancel",
                    "Save",
                    "Email explanation"
                )
            )
        )
        Assert.assertEquals(actual, expected)
    }
}
