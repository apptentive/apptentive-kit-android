package apptentive.com.android.feedback.survey

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import apptentive.com.android.feedback.survey.view.SurveyQuestionViewHolderFactory
import apptentive.com.android.feedback.survey.viewmodel.MultiChoiceQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.RangeQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SingleLineQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyFooterListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyHeaderListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.Footer
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.Header
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.RangeQuestion
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.SingleLineQuestion
import apptentive.com.android.feedback.survey.viewmodel.register
import apptentive.com.android.ui.DialogButton
import apptentive.com.android.ui.LayoutViewHolderFactory
import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.hideSoftKeyboard
import apptentive.com.android.ui.showConfirmationDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class SurveyActivity : BaseSurveyActivity() {

    private var confirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_survey)
        title = viewModel.title // So TalkBack announces the survey title

        val adapter = createAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.apptentive_survey_recycler_view)
        recyclerView.adapter = adapter

        viewModel.listItems.observe(this, Observer { items ->
            adapter.submitList(items)
        })

        viewModel.firstInvalidQuestionIndex.observe(this, Observer { firstErrorPosition ->
            if (firstErrorPosition != -1) {

                // Check if item is fully visible on screen before trying to scroll
                val layoutManger = (recyclerView.layoutManager as LinearLayoutManager)
                if (firstErrorPosition !in layoutManger.findFirstCompletelyVisibleItemPosition()..layoutManger.findLastCompletelyVisibleItemPosition()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                                if (newState == SCROLL_STATE_IDLE) {
                                    val errorView = layoutManger.findViewByPosition(firstErrorPosition)
                                    errorView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                                    recyclerView.removeOnScrollListener(this)
                                }
                            }
                        })
                    }
                    recyclerView.smoothScrollToPosition(firstErrorPosition)

                } else {
                    val errorView = layoutManger.findViewByPosition(firstErrorPosition)
                    errorView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }

            }
        })

        viewModel.exitStream.observe(this, Observer {
            finish()
        })

        viewModel.showConfirmation.observe(this, Observer {
            if (it) {
                with(viewModel.surveyCancelConfirmationDisplay) {
                    confirmationDialog = showConfirmationDialog(
                        context = this@SurveyActivity,
                        title = title ?: getString(R.string.confirmation_dialog_title),
                        message = message ?: getString(R.string.confirmation_dialog_message),
                        positiveButton = DialogButton(positiveButtonMessage ?: getString(R.string.confirmation_dialog_back_to_survey)) {
                            viewModel.onBackToSurveyFromConfirmationDialog()
                        },
                        negativeButton = DialogButton(negativeButtonMessage ?: getString(R.string.close)) {
                            viewModel.exit(showConfirmation = false)
                        }).create()
                    confirmationDialog?.show()
                }
            }
        })

        supportActionBar?.hide()

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val title = findViewById<TextView>(R.id.apptentive_survey_title)
        title.text = viewModel.title
        topAppBar.setNavigationOnClickListener {
            viewModel.exit(showConfirmation = true)
        }
    }

    override fun onBackPressed() {
        viewModel.exit(showConfirmation = true)
    }

    override fun onDestroy() {
        if (confirmationDialog?.isShowing == true) {
            confirmationDialog?.dismiss()
        }
        super.onDestroy()
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
