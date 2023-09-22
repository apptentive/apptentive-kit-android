package apptentive.com.android.feedback.utils

import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JwtUtilsTest {

    @Test
    fun testParseSubClaim() {
        val payloadJson = """
            {
                "sub": "1234567890",
                "name": "John Doe",
                "iat": 1516239022
            }
        """.trimIndent()
        val payload = JsonConverter.fromJson<JwtUtils.JwtPayload>(payloadJson)
        assertEquals("1234567890", payload.sub)
    }

    @Test
    fun testParseSubClaimMissingSub() {
        val payloadJson = """
            {
                "name": "John Doe",
                "iat": 1516239022
            }
        """.trimIndent()
        val payload = JsonConverter.fromJson<JwtUtils.JwtPayload>(payloadJson)
        assertNull(payload.sub)
    }

    @Test
    fun testExtractSubFromMultipleJwts() {
        val jwtList = listOf(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkxIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.5T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.6T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkzIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.7T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODk0IiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.8T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        )
        val expectedSubList = listOf(
            "1234567890",
            "1234567891",
            "1234567892",
            "1234567893",
            "1234567894"
        )
        for ((index, jwt) in jwtList.withIndex()) {
            val result = JwtUtils.extractSub(jwt)
            assertEquals(expectedSubList[index], result)
        }
    }

    @Test
    fun testExtractSubFromMultipleJwtsMissingSub() {
        val jwtList = listOf(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuaWNrbmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhYmMiOiIxMjM0NTY3ODkxIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.5T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhYmMiOiIxMjM0NTY3ODkyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.6T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhYmMiOiIxMjM0NTY3ODkzIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.7T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhYmMiOiIxMjM0NTY3ODk0IiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.8T4KjwRJSMfKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        )
        for (jwt in jwtList) {
            val result = JwtUtils.extractSub(jwt)
            assertNull(result)
        }
    }

    @Test
    fun testExtractSubInvalidFormat() {
        val jwt =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
        val result = JwtUtils.extractSub(jwt)
        assertNull(result)
    }
}
