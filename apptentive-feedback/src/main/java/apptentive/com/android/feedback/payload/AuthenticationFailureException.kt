package apptentive.com.android.feedback.payload

internal class AuthenticationFailureException(
    payload: PayloadData,
    val errorType: String,
    val errorMessage: String,
    cause: Throwable? = null
) : PayloadSendException(payload, errorMessage, cause)
