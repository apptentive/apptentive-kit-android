package apptentive.com.android.core.util

internal interface Factory<out T> {
    fun create(): T
}
