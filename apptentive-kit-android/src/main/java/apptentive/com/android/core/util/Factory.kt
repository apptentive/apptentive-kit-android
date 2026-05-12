package apptentive.com.android.core.util

@InternalUseOnly
interface Factory<out T> {
    fun create(): T
}
