package apptentive.com.android.network

import java.io.IOException

/**
 * Thrown to indicate a problem with the network connectivity.
 */
class NetworkUnavailableException(message: String) : IOException(message)