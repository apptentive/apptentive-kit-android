package apptentive.com.android.ui

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class DialogButton(val title: String, val action: (() -> Unit)? = null)

fun showConfirmationDialog(
    context: Context,
    title: String?,
    message: String?,
    positiveButton: DialogButton? = null,
    negativeButton: DialogButton? = null,
    neutralButton: DialogButton? = null
) {
    MaterialAlertDialogBuilder(context).apply {
        setTitle(title)
        setMessage(message)
        if (positiveButton != null) {
            setPositiveButton(positiveButton.title) { _, _ ->
                positiveButton.action?.invoke()
            }
        } else {
            setPositiveButton(android.R.string.ok, null)
        }
        if (negativeButton != null) {
            setNegativeButton(negativeButton.title) { _, _ ->
                negativeButton.action?.invoke()
            }
        }
        if (neutralButton != null) {
            setNeutralButton(neutralButton.title) { _, _ ->
                neutralButton.action?.invoke()
            }
        }
        show()
    }
}