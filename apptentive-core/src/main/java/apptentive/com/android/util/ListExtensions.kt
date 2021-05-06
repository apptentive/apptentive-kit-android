package apptentive.com.android.util

fun <T> List<T>.copyAndAdd(e: T) = List(size + 1) { index ->
    if (index < size) this[index]
    else e
}