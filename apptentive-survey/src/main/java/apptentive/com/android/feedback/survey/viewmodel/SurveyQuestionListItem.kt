package apptentive.com.android.feedback.survey.viewmodel

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.CallSuper
import apptentive.com.android.feedback.survey.view.SurveyQuestionContainerView
import apptentive.com.android.feedback.utils.HtmlWrapper.linkifiedHTMLString
import apptentive.com.android.ui.ApptentiveViewHolder
import apptentive.com.android.ui.ListViewItem

/**
 * Base class for representing question list item state
 * @param id question id
 * @param title question title
 * @param instructions optional instructions text (for example, "Required. Select between 1 and 3 answers")
 * @param validationError contains validation error message in case if the question has an invalid
 *                        answer or <code>null</code> if the answer is valid.
 */

internal abstract class SurveyQuestionListItem(
    id: String,
    type: Type,
    val title: String,
    val instructions: String?,
    val validationError: String?
) : SurveyListItem(id, type) {
    //region List View

    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        oldItem as SurveyQuestionListItem

        // check if error message changed
        if (validationError != oldItem.validationError) {
            return MASK_VALIDATION_ERROR
        }

        return 0
    }

    //endregion

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveyQuestionListItem) return false
        if (!super.equals(other)) return false

        if (title != other.title) return false
        if (instructions != other.instructions) return false
        if (validationError != other.validationError) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (instructions?.hashCode() ?: 0)
        result = 31 * result + (validationError?.hashCode() ?: 0)
        return result
    }

    //endregion

    //region String Representation

    override fun toString(): String {
        return "SurveyQuestionListItem(title='$title', instructions=$instructions, validationError=$validationError)"
    }

    //endregion

    //region Companion

    protected companion object {
        const val MASK_VALIDATION_ERROR = 0x1
        const val MASK_CUSTOM = 0x2
    }

    //endregion

    internal abstract class ViewHolder<T : SurveyQuestionListItem>(
        itemView: SurveyQuestionContainerView
    ) : ApptentiveViewHolder<T>(itemView) {
        private val containerView: SurveyQuestionContainerView = itemView
        private lateinit var _questionId: String
        protected val questionId get() = _questionId // disallow accidental modifications

        @CallSuper
        /** Called every time the adapter needs to create a list item from scratch (initial setup) */
        override fun bindView(item: T, position: Int) {
            _questionId = item.id

            // title
            containerView.title = item.title

            // instructions
            containerView.instructions = linkifiedHTMLString(item.instructions)

            // accessibility description
            val instructionDescription = if (!item.instructions.isNullOrBlank()) "${item.instructions}." else ""
            containerView.accessibilityDescription = "${item.title}. $instructionDescription"
            containerView.setQuestionContentDescription(containerView.accessibilityDescription)

            // validation error
            updateValidationError(item.validationError)

            // For Talkback: Sets focus to the question to read out error message
            if (item.validationError != null) {
                containerView.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                containerView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
            }
        }

        @CallSuper
        /** Any called if a part of the view was changed */
        override fun updateView(item: T, position: Int, changeMask: Int) {
            if (changeMask and MASK_VALIDATION_ERROR != 0) {
                updateValidationError(item.validationError)
            }
        }

        @CallSuper
        protected open fun updateValidationError(errorMessage: String?) {
            containerView.setErrorMessage(errorMessage)
        }
    }
}
