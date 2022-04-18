package apptentive.com.android.util

@InternalUseOnly
interface Factory<out T> {
    fun create(): T
}
