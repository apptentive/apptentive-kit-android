package apptentive.com.android.util

@RequiresOptIn(
    message = "This is an internal API that Apptentive library uses, please don't rely on it",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.PROPERTY
)
annotation class InternalUseOnly // Opt-in requirement annotation
