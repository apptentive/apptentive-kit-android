package apptentive.com.android.util

interface Factory<out T> {
    fun create(): T
}