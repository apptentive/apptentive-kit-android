package apptentive.com.android.serialization.json

import androidx.annotation.VisibleForTesting

/**
 * Thrown to indicate a problem with the JSON API.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
class JsonException(cause: Throwable) : Exception(cause)
