package apptentive.com.android.feedback.survey.viewmodel

import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.ListViewItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

internal class SurveyFooterListItem(
    val buttonTitle: String?,
    val disclaimerText: String?,
    val messageState: SurveySubmitMessageState? = null
) : SurveyListItem(
    id = "footer",
    type = Type.Footer
) {
    //region Equality

    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        oldItem as SurveyFooterListItem

        if (messageState != oldItem.messageState) {
            return MASK_MESSAGE_STATE
        }

        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveyFooterListItem) return false
        if (!super.equals(other)) return false

        if (buttonTitle != other.buttonTitle) return false
        if (messageState != other.messageState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (buttonTitle?.hashCode() ?: 0)
        result = 31 * result + (messageState?.hashCode() ?: 0)
        return result
    }

    //endregion

    override fun toString(): String {
        return "${javaClass.simpleName}(buttonTitle=$buttonTitle, messageState=$messageState)"
    }

    //region Companion

    private companion object {
        const val MASK_MESSAGE_STATE = 0x1
    }

    //endregion

    class ViewHolder(
        itemView: View,
        private val submitCallback: () -> Unit
    ) : ListViewAdapter.ViewHolder<SurveyFooterListItem>(itemView) {
        private val submitButton = itemView.findViewById<MaterialButton>(R.id.apptentive_submit_button)
        private val errorMessageView = itemView.findViewById<MaterialTextView>(R.id.apptentive_submit_error_message)
        private val disclaimerTextView = itemView.findViewById<MaterialTextView>(R.id.apptentive_disclaimer_text)

        init {
            submitButton.setOnClickListener {
                submitCallback.invoke()
            }
        }

        override fun bindView(item: SurveyFooterListItem, position: Int) {
            // Null checks here instead of .orEmpty() so in case text is set in customization
            if (item.buttonTitle != null) submitButton.text = item.buttonTitle
            if (item.disclaimerText != null) disclaimerTextView.text = item.disclaimerText

            updateMessageState(item.messageState)
        }

        override fun updateView(item: SurveyFooterListItem, position: Int, changeMask: Int) {
            if ((changeMask and MASK_MESSAGE_STATE) != 0) {
                updateMessageState(item.messageState)
            }
        }

        private fun updateMessageState(messageState: SurveySubmitMessageState?) {
            if (messageState != null) {
                if (messageState.isValid) {
                    Toast.makeText(errorMessageView.context, messageState.message, LENGTH_SHORT).show()
                } else {
                    errorMessageView.text = messageState.message
                    errorMessageView.visibility = View.VISIBLE
                }
            } else {
                errorMessageView.visibility = View.GONE
            }
        }
    }
}
