package apptentive.com.android.core

internal interface Factory<out T> {
    fun create(): T
}
