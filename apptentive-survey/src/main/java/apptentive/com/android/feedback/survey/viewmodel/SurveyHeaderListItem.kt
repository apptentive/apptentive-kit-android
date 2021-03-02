package apptentive.com.android.feedback.survey.viewmodel

import android.view.View
import android.widget.TextView
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.ListViewItem

class SurveyHeaderListItem(val instructions: String) : SurveyListItem(
    id = "header",
    type = Type.Header
) {
    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        return 0; // this item never changes dynamically
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveyHeaderListItem) return false
        if (!super.equals(other)) return false

        if (instructions != other.instructions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + instructions.hashCode()
        return result
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(instructions=$instructions)"
    }

    class ViewHolder(itemView: View) : ListViewAdapter.ViewHolder<SurveyHeaderListItem>(itemView) {
        private val introductionView: TextView = itemView.findViewById(R.id.survey_introduction)

        override fun bindView(item: SurveyHeaderListItem, position: Int) {
            introductionView.text = item.instructions
        }
    }
}