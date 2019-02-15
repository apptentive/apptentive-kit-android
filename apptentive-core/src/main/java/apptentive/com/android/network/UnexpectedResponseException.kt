package apptentive.com.android.network

/**
 * Thrown to indicate an unexpected HTTP-response.
 */
class UnexpectedResponseException(val statusCode: Int, val statusMessage: String, val errorMessage: String? = null) :
    Exception("Unexpected response $statusCode ($statusMessage)${if (errorMessage?.isNotEmpty() == true) ": $errorMessage" else ""}")