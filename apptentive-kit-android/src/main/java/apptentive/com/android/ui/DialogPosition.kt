package apptentive.com.android.ui

import android.view.Gravity

internal enum class DialogPosition {
    TOP, CENTER, BOTTOM
}

internal fun String.toDialogPosition(): DialogPosition {
    return when (this) {
        "top" -> DialogPosition.TOP
        "center" -> DialogPosition.CENTER
        "bottom" -> DialogPosition.BOTTOM
        else -> DialogPosition.CENTER
    }
}

internal fun DialogPosition.toGravity() = when (this) {
    DialogPosition.TOP -> Gravity.TOP
    DialogPosition.CENTER -> Gravity.CENTER
    DialogPosition.BOTTOM -> Gravity.BOTTOM
}
