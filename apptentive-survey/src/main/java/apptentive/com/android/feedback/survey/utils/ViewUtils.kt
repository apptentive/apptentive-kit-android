package apptentive.com.android.feedback.survey.utils

import com.google.android.material.textfield.TextInputEditText

/**
 *  "Fix" for TextInputLayout focused text-less box stroke color.
 *  When android:background is set in styles, the state mentioned above will be
 *  the same color as that background and will appear invisible.
 *  This "fix" adds a space when the box is empty and focused and then sets the
 *  cursor to the first position.
 *
 *  Trims any white space when unfocused.
 *
 *  Second part to this fix is to put something like
 *  textInputEditText.hasFocus() && it.isNullOrEmpty() -> textInputEditText.setText(" ")
 *  into doAfterTextChanged. This cannot be done here because of varying callbacks.
 *  This second part will ensure there is always at least a space even if the user deletes it.
 *
 *  Side effect: TalkBack says "space" upon entering the field for the first time.
 *
 *  Uses:
 *  @see apptentive.com.android.feedback.survey.viewmodel.SingleLineQuestionListItem.ViewHolder.bindView
 *  @see apptentive.com.android.feedback.survey.viewmodel.MultiChoiceQuestionListItem.ViewHolder.bindView
 */
internal fun TextInputEditText.setTextBoxBackgroundFocusFix() {
    setOnFocusChangeListener { _, hasFocus ->
        when {
            hasFocus && text.isNullOrEmpty() -> {
                setText(" ")
                post { setSelection(0) }
            }
            text != null -> setText(text?.trim())
        }
    }
}