package apptentive.com.android.feedback.conversation

import apptentive.com.android.core.ApptentiveException

internal class ConversationSerializationException(message: String, cause: Throwable?) : ApptentiveException(message, cause)
internal class ConversationLoggedOutException(message: String, cause: Throwable?) : ApptentiveException(message, cause)
