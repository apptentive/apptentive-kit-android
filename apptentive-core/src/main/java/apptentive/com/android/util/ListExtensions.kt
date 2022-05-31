package apptentive.com.android.util

@InternalUseOnly
fun <T> List<T>.copyAndAdd(e: T) = List(size + 1) { index ->
    if (index < size) this[index]
    else e
}

@InternalUseOnly
fun <T> List<T>.isSame(newList: List<T>): Boolean =
    containsAll(newList) && newList.containsAll(this)
