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

internal class SurveyIntroductionPageItem(val introduction: String, val disclaimer: String) : SurveyListItem(
    id = "introduction",
    type = Type.Introduction
) {
    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        return 0 // this item never changes dynamically
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveyIntroductionPageItem) return false
        if (!super.equals(other)) return false

        if (introduction != other.introduction) return false
        if (disclaimer != other.disclaimer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + introduction.hashCode()
        result = 31 * result + disclaimer.hashCode()
        return result
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(introduction=$introduction, disclaimer=$disclaimer)"
    }

    class ViewHolder(itemView: View) : ApptentiveViewHolder<SurveyIntroductionPageItem>(itemView) {
        private val introductionView = itemView.findViewById<MaterialTextView>(R.id.apptentive_survey_introduction)
        private val disclaimerView = itemView.findViewById<MaterialTextView>(R.id.apptentive_survey_disclaimer)

        override fun bindView(item: SurveyIntroductionPageItem, position: Int) {
            introductionView.text = item.introduction
            disclaimerView.text = item.disclaimer

            if (item.introduction.isBlank()) introductionView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            if (item.disclaimer.isBlank()) disclaimerView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            try {
                Linkify.addLinks(introductionView, Linkify.ALL)
                introductionView.movementMethod = LinkMovementMethod.getInstance()
                Linkify.addLinks(disclaimerView, Linkify.ALL)
                disclaimerView.movementMethod = LinkMovementMethod.getInstance()
            } catch (exception: Exception) {
                Log.e(SURVEY, "Couldn't add linkify to survey introduction or disclaimer text", exception)
            }
        }
    }
}
