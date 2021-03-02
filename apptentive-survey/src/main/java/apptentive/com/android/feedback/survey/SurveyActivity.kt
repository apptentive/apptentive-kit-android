package apptentive.com.android.feedback.survey

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.survey.view.SurveyQuestionViewHolderFactory
import apptentive.com.android.feedback.survey.viewmodel.MultiChoiceQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.RangeQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SingleLineQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyFooterListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyHeaderListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.*
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.feedback.survey.viewmodel.register
import apptentive.com.android.ui.*
import kotlinx.android.synthetic.main.apptentive_activity_survey.*

class SurveyActivity : ApptentiveViewModelActivity<SurveyViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_survey)

        val adapter = createAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.apptentive_survey_recycler_view)
        recyclerView.adapter = adapter

        viewModel.listItems.observe(this, Observer { items ->
            adapter.submitList(items)
        })

        viewModel.exitStream.observe(this, Observer {
            finish()
        })

        viewModel.showConfirmation.observe(this, Observer {
            showConfirmationDialog(
                context = this,
                title = getString(R.string.confirmation_dialog_title),
                message = getString(R.string.confirmation_dialog_message),
                positiveButton = DialogButton(getString(R.string.confirmation_dialog_back_to_survey)),
                negativeButton = DialogButton(getString(R.string.close)) {
                    viewModel.exit(showConfirmation = false)
                }
            )
        })

        supportActionBar?.hide()

        topAppBar.title = viewModel.title
        topAppBar.setNavigationOnClickListener {
            viewModel.exit(showConfirmation = true)
        }
    }

    override fun onBackPressed() {
        viewModel.exit(showConfirmation = true)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // We need to remove focus from any EditText when user touches outside
        // of it. Otherwise, the focus would weirdly jump while scrolling through items.
        // see: https://stackoverflow.com/a/28939113
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            if (focusedView is EditText) {
                val outRect = Rect()
                focusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    focusedView.clearFocus()
                    focusedView.hideSoftKeyboard()
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun createAdapter() = ListViewAdapter().apply {
        // Header view
        register(
            type = Header,
            factory = LayoutViewHolderFactory(R.layout.apptentive_survey_header) {
                SurveyHeaderListItem.ViewHolder(it)
            }
        )

        // Single Line Question
        register(
            type = SingleLineQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_singleline) {
                SingleLineQuestionListItem.ViewHolder(it) { questionId, text ->
                    viewModel.updateAnswer(questionId, text)
                }
            }
        )
        // Range Question
        register(
            type = RangeQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_range) {
                RangeQuestionListItem.ViewHolder(it) { questionId, selectedIndex ->
                    viewModel.updateAnswer(questionId, selectedIndex)
                }
            }
        )
        // Multi-choice Question
        register(
            type = MultiChoiceQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_multichoice) {
                MultiChoiceQuestionListItem.ViewHolder(it) { questionId, choiceId, selected, text ->
                    viewModel.updateAnswer(questionId, choiceId, selected, text)
                }
            }
        )

        // Submit button
        register(
            type = Footer,
            factory = LayoutViewHolderFactory(R.layout.apptentive_survey_footer) {
                SurveyFooterListItem.ViewHolder(it) {
                    viewModel.submit()
                }
            }
        )
    }
}