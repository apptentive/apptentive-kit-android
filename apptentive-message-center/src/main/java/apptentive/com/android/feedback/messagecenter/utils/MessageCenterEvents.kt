package apptentive.com.android.feedback.messagecenter.utils

// Message center internal events
internal object MessageCenterEvents {
    val EVENT_NAME_CLOSE = "close" // Closed from X in toolbar
    val EVENT_NAME_CANCEL = "cancel" // Closed from back button
    val EVENT_NAME_SEND = "send" // Message was sent
    val EVENT_NAME_READ = "read" // Message was read
    val EVENT_NAME_STATUS = "status" // When there is a messageSLA
    val EVENT_NAME_PROFILE_OPEN = "profile_open" // Profile screen button pressed
    val EVENT_NAME_PROFILE_CLOSE = "profile_close" // Profile screen closed
    val EVENT_NAME_PROFILE_SUBMIT = "profile_submit" // Profile updated
    val EVENT_NAME_ATTACH = "attach" // Attachment added
    val EVENT_NAME_ATTACHMENT_DELETE = "attachment_delete" // Attachment removed
    val EVENT_NAME_ATTACHMENT_CANCEL = "attachment_cancel" // Attachment picker cancelled

    /* Parity with Legacy
    "Not implemented on Android"
    val EVENT_NAME_GREETING_MESSAGE = "greeting_message"
    val EVENT_NAME_KEYBOARD_OPEN = "keyboard_open"
    val EVENT_NAME_KEYBOARD_CLOSE = "keyboard_close"
    val EVENT_NAME_PROFILE_NAME = "profile_name"
    val EVENT_NAME_PROFILE_EMAIL = "profile_email"

    // These are used in Legacy, but UI doesn't have open / close compose functionality
    val EVENT_NAME_COMPOSE_OPEN = "compose_open" // N/A. Legacy has compose FAB
    val EVENT_NAME_COMPOSE_CLOSE = "compose_close" // N/A. Legacy can close creating message
    val EVENT_NAME_MESSAGE_NETWORK_ERROR = "message_network_error" // Network errors are handled differently
    */
}
