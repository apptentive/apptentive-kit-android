package apptentive.com.android.feedback

import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION

interface AuthenticationFailedListener {
    fun onAuthenticationFailed(reason: AuthenticationFailedReason)
}

/**
 * A list of error codes you will encounter when a JWT failure for logged in conversations occurs.
 */
enum class AuthenticationFailedReason(val message: String? = null) {
    /**
     * This should not happen.
     */
    UNKNOWN,

    /**
     * Currently only the HS512 signature algorithm is supported.
     */
    INVALID_ALGORITHM,

    /**
     * The JWT structure is constructed improperly (missing a part, etc.)
     */
    MALFORMED_TOKEN,

    /**
     * The token is not signed properly, or can't be decoded.
     */
    INVALID_TOKEN,

    /**
     * There is no "sub" property in the JWT claims. The "sub" is required, and should be an
     * immutable, unique id for your user.
     */
    MISSING_SUB_CLAIM,

    /**
     * The JWT "sub" claim does not match the one previously registered to the internal Apptentive
     * conversation. Internal use only.
     */
    MISMATCHED_SUB_CLAIM,

    /**
     * Internal use only.
     */
    INVALID_SUB_CLAIM,

    /**
     * The expiration "exp" claim is expired. The "exp" claim is a UNIX timestamp in milliseconds.
     * The JWT will receive this authentication failure when the "exp" time has elapsed.
     */
    EXPIRED_TOKEN("TOKEN IS EXPIRED"),

    /**
     * The JWT has been revoked. This happens after a successful logout. In such cases, you will
     * need a new JWT to login.
     */
    REVOKED_TOKEN,

    /**
     * The Apptentive Key field was not specified during registration. You can get this from your app's Apptentive
     * settings.
     */
    MISSING_APP_KEY,

    /**
     * The Apptentive Signature field was not specified during registration. You can get this from your app's Apptentive
     * settings.
     */
    MISSING_APP_SIGNATURE,

    /**
     * The Apptentive Key and Apptentive Signature fields do not match. Make sure you got them from
     * the same app's Apptentive settings page.
     */
    INVALID_KEY_SIGNATURE_PAIR;

    private var error: String? = null
    fun error(): String? {
        return error
    }

    override fun toString(): String {
        return "AuthenticationFailedReason{" +
            "error='" + error + '\'' +
            "errorType='" + name + '\'' +
            '}'
    }

    companion object {
        fun parse(errorType: String, error: String): AuthenticationFailedReason {
            try {
                when {
                    errorType.isEmpty() && error.isEmpty() -> return UNKNOWN
                    errorType.isNotEmpty() -> return values().first { it.message == errorType.uppercase() }.apply {
                        this.error = error
                    }
                    error.isNotEmpty() -> return values().first {
                        it.message == error.uppercase()
                    }.apply {
                        this.error = error
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    CONVERSATION,
                    "Error parsing unknown Apptentive.AuthenticationFailedReason: $errorType"
                )
            }
            return UNKNOWN
        }
    }
}
