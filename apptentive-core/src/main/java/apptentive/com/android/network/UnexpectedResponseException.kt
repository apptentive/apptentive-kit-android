package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly

/**
 * Thrown to indicate an unexpected HTTP-response.
 */
@InternalUseOnly
class UnexpectedResponseException(val statusCode: Int, val statusMessage: String, val errorMessage: String? = null) :
    Exception("Unexpected response $statusCode ($statusMessage)${if (errorMessage?.isNotEmpty() == true) ": $errorMessage" else ""}")
