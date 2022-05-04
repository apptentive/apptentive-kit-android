package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly

/**
 * Thrown to indicate an unexpected 400-499 responses.
 */
@InternalUseOnly
class SendErrorException(val statusCode: Int, val statusMessage: String, val errorMessage: String? = null) :
    Exception("Send error: $statusCode ($statusMessage)${if (errorMessage?.isNotEmpty() == true) ": $errorMessage" else ""}")
