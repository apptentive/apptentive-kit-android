package apptentive.com.android.core

/** Provider interface which is responsible for getting instances of the particular class. */
interface Provider<T> {
    fun get(): T
}