package apptentive.com.android.feedback.utils

import android.util.Base64
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
import java.lang.Exception

internal typealias JwtString = String

internal object JwtUtils {
    private const val EXPECTED_JWT_PARTS = 3
    private const val JWT_PART_PAYLOAD = 1

    fun extractSub(jwt: String): JwtString? {
        // JWT parts are header, payload, signature, and always in that order
        val parts = jwt.split(".")
        if (parts.size != EXPECTED_JWT_PARTS) {
            Log.w(CONVERSATION, "Invalid JWT format: expected 3 parts, found ${parts.size}")
            return null
        }

        // JWT payloads contain a sub, name, and iat
        val payloadJson = try {
            // The payload is the middle part, and is base64 encoded
            String(Base64.decode(parts[JWT_PART_PAYLOAD], Base64.URL_SAFE))
        } catch (e: IllegalArgumentException) {
            Log.w(CONVERSATION, "Invalid JWT payload: ${e.message}")
            return null
        }

        // The JWT sub is the user's unique identifier, and the only one we care about
        return try {
            JsonConverter.fromJson<JwtPayload>(payloadJson).sub
        } catch (e: Exception) {
            Log.w(CONVERSATION, "Error parsing JWT payload: ${e.message}")
            return null
        }
    }

    data class JwtPayload(val sub: String?)
}
