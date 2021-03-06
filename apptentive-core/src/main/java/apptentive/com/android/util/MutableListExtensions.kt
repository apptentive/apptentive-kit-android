package apptentive.com.android.util

internal fun <T> MutableList<T>.remove(predicate: (T) -> Boolean): T? {
    val index = indexOfFirst(predicate)
    return if (index != -1) removeAt(index) else null
}
