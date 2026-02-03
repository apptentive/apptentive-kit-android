package apptentive.com.app

import android.app.Application
import android.content.Context
import android.util.Log
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.feedback.ApptentiveRegion
import apptentive.com.android.feedback.AuthenticationFailedListener
import apptentive.com.android.feedback.AuthenticationFailedReason
import apptentive.com.android.feedback.LoginResult
import apptentive.com.android.feedback.RegisterResult
import apptentive.com.android.util.LogLevel
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import java.io.UnsupportedEncodingException
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.crypto.spec.SecretKeySpec

val configuration = ApptentiveConfiguration(
    BuildConfig.APPTENTIVE_KEY,
    BuildConfig.APPTENTIVE_SIGNATURE
).apply {
    shouldInheritAppTheme = false
    logLevel = LogLevel.Verbose
    customAppStoreURL = "https://play.google.com/store/apps/details?id=com.apptentive.dogfacts"
    ratingInteractionThrottleLength = TimeUnit.SECONDS.toMillis(30)
}

class MyApplication : Application(), AuthenticationFailedListener {
    override fun onCreate() {
        val prefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        // Turning off by default to get un-redacted logs
        configuration.shouldSanitizeLogMessages = prefs.getBoolean(SHOULD_SANITIZE, false)

        configuration.shouldEncryptStorage = prefs.getBoolean(SHOULD_ENCRYPT, true)

        configuration.region = ApptentiveRegion.US

        super.onCreate()
        Apptentive.register(this, configuration) { it ->
            when (it) {
                RegisterResult.Success -> {
                    Log.v("SYSTEM", "Registration successful")
                    Apptentive.rebootSDKSubjectObservable.observe {
                        if (it == true) {
                            Apptentive.rebootSDK(this, configuration) { result ->
                                when (result) {
                                    RegisterResult.Success -> Log.v("SYSTEM", "Reboot successful")
                                    is RegisterResult.Failure -> Log.e("SYSTEM", "Reboot failed")
                                    is RegisterResult.Exception -> Log.e(
                                        "SYSTEM",
                                        "Reboot exception"
                                    )
                                }
                            }
                        }
                    }
                }
                is RegisterResult.Failure -> Log.e(
                    "SYSTEM",
                    "Registration failed with response code: ${it.responseCode} and error message: ${it.message}"
                )
                is RegisterResult.Exception -> Log.e(
                    "SYSTEM",
                    "Registration failed with exception: ${it.error}"
                )
            }
        }

        Apptentive.setAuthenticationFailedListener(this)
    }

    override fun onAuthenticationFailed(reason: AuthenticationFailedReason) {
        Log.e("Apptentive", "Authentication failed: $reason")
        Log.d("Apptentive", "Updating token")
        Apptentive.updateToken(
            generateJWT(
                subject = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).getString("USER_NAME", "Poorni"),
                "ClientTeam", System.currentTimeMillis(),
                System.currentTimeMillis() + (DevFunctionsActivity.ONE_DAY * 30),
                "38127017f4cfb4f84c8dfecd48ab98c6",
                null,
                null
            )!!
        ) {
            when (it) {
                is LoginResult.Success -> {
                    Log.d("Apptentive", "update token success: $reason")
                }
                is LoginResult.Error -> {
                    Log.e("Apptentive", "update token error: ${it.message}")
                }
                is LoginResult.Failure -> {
                    Log.e("Apptentive", "update token failure: ${it.message}")
                }
                is LoginResult.Exception -> {
                    Log.e("Apptentive", "update token exception: ${it.error.message}")
                }
            }
        }
    }

    companion object {
        internal fun generateJWT(
            subject: String?,
            issuer: String,
            issuedAt: Long,
            expiration: Long,
            secret: String?,
            headerParams: Map<String, Any>?,
            bodyParams: Map<String, Any>?
        ): String? {
            if (secret.isNullOrEmpty()) {
                Log.e("APPTENTIVE", "Missing Secret")
                return null
            }
            val secretKey: SecretKeySpec = try {
                SecretKeySpec(secret.toByteArray(charset("UTF-8")), "HmacSHA512")
            } catch (e: UnsupportedEncodingException) {
                Log.e("APPTENTIVE", "Error generating JWT: " + e.message)
                return null
            }
            val builder: JwtBuilder = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("type", "user")
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Date(issuedAt))
                .setExpiration(Date(expiration))
                .setHeaderParams(headerParams)
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, secretKey)
            if (bodyParams != null) {
                for (key in bodyParams.keys) {
                    builder.claim(key, bodyParams[key])
                }
            }
            return builder.compact()
        }
    }
}
