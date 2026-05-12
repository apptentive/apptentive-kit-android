package apptentive.com.android.feedback

/**
 * Result class which represents callback result for `login` method
 *
 * [Success] - login was successful
 *
 * [Failure] - login request was attempted but failed with response code
 *
 * [Exception] - during the login process a exception was thrown
 *
 * [Error] - either the passed jwt token was invalid or the SDK is in a state like already logged in
 * or not initialized yet and therefore cannot login
 *
 */
sealed class LoginResult {
    object Success : LoginResult()
    data class Failure(val message: String, val responseCode: Int) : LoginResult()
    data class Exception(val error: Throwable) : LoginResult()
    data class Error(val message: String) : LoginResult()
}
