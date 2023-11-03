package apptentive.com.android.feedback.payload

internal class AuthenticationFailureException(
    payload: PayloadData,
    val errorMessage: String? = null,
    cause: Throwable? = null
) : PayloadSendException(payload, errorMessage, cause)
