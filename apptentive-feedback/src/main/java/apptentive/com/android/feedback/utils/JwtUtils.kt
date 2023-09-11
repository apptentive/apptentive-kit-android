package apptentive.com.android.feedback.utils

import android.util.Base64
import apptentive.com.android.serialization.json.JsonConverter
import java.lang.Exception

internal sealed class JwtResult {
    data class Success(val sub: String) : JwtResult()
    data class Error(val message: String) : JwtResult()
}

internal object JwtUtils {
    private const val EXPECTED_JWT_PARTS = 3
    private const val JWT_PART_PAYLOAD = 1

    fun extractSub(jwt: String): JwtResult {
        // JWT parts are header, payload, signature, and always in that order
        val parts = jwt.split(".")
        if (parts.size != EXPECTED_JWT_PARTS) {
            return JwtResult.Error("Invalid JWT format: expected 3 parts, found ${parts.size}")
        }

        // JWT payloads contain a sub, name, and iat
        val payloadJson = try {
            // The payload is the middle part, and is base64 encoded
            String(Base64.decode(parts[JWT_PART_PAYLOAD], Base64.URL_SAFE))
        } catch (e: IllegalArgumentException) {
            return JwtResult.Error("Invalid JWT payload: ${e.message}")
        }

        // The JWT sub is the user's unique identifier, and the only one we care about
        val sub = try {
            JsonConverter.fromJson<JwtPayload>(payloadJson).sub
        } catch (e: Exception) {
            return JwtResult.Error("Error parsing JWT payload: ${e.message}")
        }

        return if (sub != null) {
            JwtResult.Success(sub)
        } else {
            JwtResult.Error("JWT payload does not contain 'sub' claim")
        }
    }

    data class JwtPayload(val sub: String?)
}
