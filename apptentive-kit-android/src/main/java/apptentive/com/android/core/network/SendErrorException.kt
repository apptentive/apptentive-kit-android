package apptentive.com.android.core.network

import apptentive.com.android.core.util.InternalUseOnly

/**
 * Thrown to indicate an unexpected 400-499 responses.
 */
@InternalUseOnly
class SendErrorException(
    val statusCode: Int,
    val statusMessage: String,
    val errorType: String? = null,
    val errorMessage: String? = null
) :
    Exception("Send error: $statusCode ($statusMessage)${if (errorMessage?.isNotEmpty() == true) ": $errorMessage" else ""}")
