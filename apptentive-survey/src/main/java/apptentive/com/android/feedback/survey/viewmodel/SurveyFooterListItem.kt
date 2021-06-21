package apptentive.com.android.feedback.survey.viewmodel

import android.view.View
import android.widget.TextView
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.ListViewItem
import apptentive.com.android.ui.getThemeColor

class SurveyFooterListItem(
    val buttonTitle: String?,
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
        private val submitButton: TextView = itemView.findViewById(R.id.submit_button)
        private val messageView: TextView = itemView.findViewById(R.id.success_error_message)

        init {
            submitButton.setOnClickListener {
                submitCallback.invoke()
            }
        }

        override fun bindView(item: SurveyFooterListItem, position: Int) {
            if (item.buttonTitle != null) {
                submitButton.text = item.buttonTitle
            }
            updateMessageState(item.messageState)
        }

        override fun updateView(item: SurveyFooterListItem, position: Int, changeMask: Int) {
            if ((changeMask and MASK_MESSAGE_STATE) != 0) {
                updateMessageState(item.messageState)
            }
        }

        private fun updateMessageState(messageState: SurveySubmitMessageState?) {
            if (messageState != null) {
                messageView.text = messageState.message
                val colorId =
                    if (messageState.isValid) R.attr.colorOnBackground else R.attr.colorError
                messageView.setTextColor(messageView.context.getThemeColor(colorId))
                messageView.visibility = View.VISIBLE
            } else {
                messageView.visibility = View.INVISIBLE
            }
        }
    }
}