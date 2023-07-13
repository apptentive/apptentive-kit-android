package apptentive.com.android.feedback.survey.viewmodel

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.View
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.ui.ApptentiveViewHolder
import apptentive.com.android.ui.ListViewItem
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.SURVEY
import com.google.android.material.textview.MaterialTextView

internal class SurveySuccessPageItem(val success: String, val disclaimer: String) : SurveyListItem(
    id = "success",
    type = Type.Success
) {
    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        return 0 // this item never changes dynamically
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveySuccessPageItem) return false
        if (!super.equals(other)) return false

        if (success != other.success) return false
        if (disclaimer != other.disclaimer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + success.hashCode()
        result = 31 * result + disclaimer.hashCode()
        return result
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(success=$success, disclaimer=$disclaimer)"
    }

    class ViewHolder(itemView: View) : ApptentiveViewHolder<SurveySuccessPageItem>(itemView) {
        private val successView = itemView.findViewById<MaterialTextView>(R.id.apptentive_survey_success)
        private val disclaimerView = itemView.findViewById<MaterialTextView>(R.id.apptentive_survey_disclaimer)

        override fun bindView(item: SurveySuccessPageItem, position: Int) {
            successView.text = item.success
            disclaimerView.text = item.disclaimer

            if (item.disclaimer.isBlank()) disclaimerView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            try {
                Linkify.addLinks(successView, Linkify.ALL)
                successView.movementMethod = LinkMovementMethod.getInstance()
                Linkify.addLinks(disclaimerView, Linkify.ALL)
                disclaimerView.movementMethod = LinkMovementMethod.getInstance()
            } catch (exception: Exception) {
                Log.e(SURVEY, "Couldn't add linkify to survey success or disclaimer text", exception)
            }
        }
    }
}
