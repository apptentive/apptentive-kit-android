package apptentive.com.android.util

@InternalUseOnly
fun isAllNull(vararg args: Any?): Boolean {
    return args.all { it == null }
}

@InternalUseOnly
fun isAnyNull(vararg args: Any?): Boolean {
    return args.any { it == null }
}

@InternalUseOnly
fun isNoneNull(vararg args: Any?): Boolean {
    return args.all { it != null }
}
