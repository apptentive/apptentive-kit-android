package apptentive.com.android.feedback

import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION

interface AuthenticationFailedListener {
    fun onAuthenticationFailed(reason: AuthenticationFailedReason)
}

/**
 * A list of error codes you will encounter when a JWT failure for logged in conversations occurs.
 */
enum class AuthenticationFailedReason(val description: String) {
    /**
     * This should not happen.
     */
    UNKNOWN("Unknown reason"),

    /**
     * Currently only the HS512 signature algorithm is supported.
     */
    INVALID_ALGORITHM("Invalid algorithm"),

    /**
     * The JWT structure is constructed improperly (missing a part, etc.)
     */
    MALFORMED_TOKEN("Malformed token"),

    /**
     * The token is not signed properly, or can't be decoded.
     */
    INVALID_TOKEN("Invalid token"),

    /**
     * There is no "sub" property in the JWT claims. The "sub" is required, and should be an
     * immutable, unique id for your user.
     */
    MISSING_SUB_CLAIM("Missing sub claim"),

    /**
     * The JWT "sub" claim does not match the one previously registered to the internal Apptentive
     * conversation. Internal use only.
     */
    MISMATCHED_SUB_CLAIM("Mismatched sub claim"),

    /**
     * Internal use only.
     */
    INVALID_SUB_CLAIM("Invalid sub claim"),

    /**
     * The expiration "exp" claim is expired. The "exp" claim is a UNIX timestamp in milliseconds.
     * The JWT will receive this authentication failure when the "exp" time has elapsed.
     */
    EXPIRED_TOKEN("Expired token"),

    /**
     * The JWT has been revoked. This happens after a successful logout. In such cases, you will
     * need a new JWT to login.
     */
    REVOKED_TOKEN("Revoked token"),

    /**
     * The Apptentive Key field was not specified during registration. You can get this from your app's Apptentive
     * settings.
     */
    MISSING_APP_KEY("Missing app key"),

    /**
     * The Apptentive Signature field was not specified during registration. You can get this from your app's Apptentive
     * settings.
     */
    MISSING_APP_SIGNATURE("Missing app signature"),

    /**
     * The Apptentive Key and Apptentive Signature fields do not match. Make sure you got them from
     * the same app's Apptentive settings page.
     */
    INVALID_KEY_SIGNATURE_PAIR("Invalid key/signature pair");

    override fun toString(): String {
        return "AuthenticationFailedReason{" +
            "error='" + name + '\'' +
            '}'
    }

    companion object {
        fun parse(error: String): AuthenticationFailedReason {
            try {
                val ret: AuthenticationFailedReason =
                    AuthenticationFailedReason.valueOf(
                        error
                    )
                return ret
            } catch (e: Exception) {
                Log.e(
                    CONVERSATION,
                    "Error parsing unknown Apptentive.AuthenticationFailedReason: $error"
                )
            }
            return UNKNOWN
        }
    }
}
