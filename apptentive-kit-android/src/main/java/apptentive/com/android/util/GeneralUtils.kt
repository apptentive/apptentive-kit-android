package apptentive.com.android.util

internal fun isAllNull(vararg args: Any?): Boolean {
    return args.all { it == null }
}

internal fun isAnyNull(vararg args: Any?): Boolean {
    return args.any { it == null }
}

internal fun isNoneNull(vararg args: Any?): Boolean {
    return args.all { it != null }
}
