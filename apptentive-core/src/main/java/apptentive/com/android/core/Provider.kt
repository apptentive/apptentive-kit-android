package apptentive.com.android.core

interface Provider<T> {
    fun get(): T
}