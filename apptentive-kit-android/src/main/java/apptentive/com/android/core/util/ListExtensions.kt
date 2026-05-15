package apptentive.com.android.core.util

internal fun <T> List<T>.copyAndAdd(e: T) = List(size + 1) { index ->
    if (index < size) this[index]
    else e
}

internal fun <T> List<T>.isSame(newList: List<T>): Boolean =
    containsAll(newList) && newList.containsAll(this)

internal fun <T> List<T>?.isNotNullOrEmpty() = !isNullOrEmpty()
