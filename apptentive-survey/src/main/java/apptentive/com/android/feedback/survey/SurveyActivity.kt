package apptentive.com.android.feedback.survey

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.survey.view.SurveyQuestionViewHolderFactory
import apptentive.com.android.feedback.survey.viewmodel.MultiChoiceQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.RangeQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SingleLineQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyQuestionListItem.Type.*
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.feedback.survey.viewmodel.register
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.hideSoftKeyboard

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

        viewModel.validationErrorText.observe(this, Observer { errorText ->
            if (errorText != null) {
                // TODO: show customizable toast
                Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.exitStream.observe(this, Observer {
            finish()
        })

        val sendButton = findViewById<Button>(R.id.send)
        sendButton.setOnClickListener {
            viewModel.submit()
        }
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
    }
}