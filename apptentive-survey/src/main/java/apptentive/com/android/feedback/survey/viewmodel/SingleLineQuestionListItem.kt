package apptentive.com.android.feedback.survey.viewmodel

import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.START
import android.view.Gravity.TOP
import android.view.inputmethod.EditorInfo.TYPE_CLASS_TEXT
import android.view.inputmethod.EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.view.inputmethod.EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
import androidx.core.widget.doAfterTextChanged
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.feedback.survey.view.SurveyQuestionContainerView
import apptentive.com.android.ui.setInvalid
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Class which represents single line question list item state
 * @param id question id
 * @param title question title
 * @param instructions optional instructions text (for example, "Required")
 * @param validationError contains validation error message in case if the question has an invalid answer or `null` if the answer is valid.
 * @param text user answer text
 * @param freeFormHint hint text to be displayed if user provided no answer
 * @param multiline indicates if the answer field should occupy more than a single line
 */
internal class SingleLineQuestionListItem(
    id: String,
    title: String,
    instructions: String? = null,
    validationError: String? = null,
    val text: String = "",
    val freeFormHint: String? = null,
    val multiline: Boolean = false
) : SurveyQuestionListItem(
    id = id,
    type = Type.SingleLineQuestion,
    title = title,
    instructions = instructions,
    validationError = validationError
) {
    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SingleLineQuestionListItem) return false
        if (!super.equals(other)) return false

        if (text != other.text) return false
        if (freeFormHint != other.freeFormHint) return false
        if (multiline != other.multiline) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (freeFormHint?.hashCode() ?: 0)
        result = 31 * result + multiline.hashCode()
        return result
    }

    //endregion

    //region String Representation

    override fun toString(): String {
        return "SingleLineQuestionListItem(title='$title', instructions=$instructions, validationError=$validationError text='$text', freeformHint=$freeFormHint, multiline=$multiline)"
    }

    //endregion

    //region View Holder
    class ViewHolder(
        itemView: SurveyQuestionContainerView,
        private val isPaged: Boolean = false,
        val onTextChanged: (id: String, text: String) -> Unit
    ) : SurveyQuestionListItem.ViewHolder<SingleLineQuestionListItem>(itemView) {
        private val answerTextInputLayout: TextInputLayout = itemView.findViewById(R.id.apptentive_answer_text_input_layout)
        private val answerEditText: TextInputEditText = itemView.findViewById(R.id.apptentive_answer_text)

        override fun bindView(
            item: SingleLineQuestionListItem,
            position: Int
        ) {
            super.bindView(item, position)

            answerTextInputLayout.hint = item.freeFormHint

            // look-and-feel
            if (item.multiline) {
                answerTextInputLayout.gravity = TOP or START
                answerEditText.gravity = TOP or START
                answerEditText.inputType =
                    TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_MULTI_LINE
                answerEditText.minLines = 4
                answerEditText.maxLines = 8
            } else {
                answerTextInputLayout.gravity = CENTER_VERTICAL or START
                answerEditText.gravity = CENTER_VERTICAL or START
                answerEditText.inputType = TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES
                answerEditText.minLines = 1
                answerEditText.maxLines = 5
            }

            answerEditText.setText(item.text)
            answerEditText.doAfterTextChanged {
                onTextChanged(questionId, it?.toString().orEmpty().trim())
            }

            // Fix for ViewPager adapter updating the view on error and resetting the cursor position
            if (isPaged) {
                answerEditText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) updateValidationError(null)
                }
            }
        }

        override fun updateValidationError(errorMessage: String?) {
            super.updateValidationError(errorMessage)
            answerTextInputLayout.setInvalid(errorMessage != null)
        }
    }

    //endregion
}
