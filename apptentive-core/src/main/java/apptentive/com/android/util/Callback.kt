package apptentive.com.android.util

interface Callback<in T> {
    fun onComplete(t: T)
    fun onFailure(t: Throwable)
}