package apptentive.com.android.feedback.survey.view

import android.view.View
import android.view.ViewGroup
import apptentive.com.android.feedback.survey.viewmodel.SurveyQuestionListItem
import apptentive.com.android.ui.ViewHolderFactory

internal class SurveyQuestionViewHolderFactory(
    private val layoutId: Int,
    private val viewHolderCreator: (SurveyQuestionContainerView) -> SurveyQuestionListItem.ViewHolder<*>
) : ViewHolderFactory {
    override fun createItemView(parent: ViewGroup): View {
        val containerView = SurveyQuestionContainerView(parent.context)
        containerView.setAnswerView(layoutId)
        return containerView
    }

    override fun createViewHolder(itemView: View) =
        viewHolderCreator(itemView as SurveyQuestionContainerView)
}
