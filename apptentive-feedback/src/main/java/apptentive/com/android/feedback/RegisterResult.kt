package apptentive.com.android.feedback

/** Sealed class which represents callback result for the method
 * {@link Apptentive#register(Application, ApptentiveConfiguration, RegisterCallback)}
 *
 */
sealed class RegisterResult {
    object Success : RegisterResult()
    data class Failure(val message: String, val responseCode: Int) : RegisterResult()
    // TODO: can elaborate types of Failure based on response code
    data class Exception(val error: Throwable) : RegisterResult()
}
