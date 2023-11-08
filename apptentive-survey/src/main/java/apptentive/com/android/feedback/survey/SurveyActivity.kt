package apptentive.com.android.feedback.survey

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.viewpager2.widget.ViewPager2
import apptentive.com.android.feedback.survey.view.SurveyQuestionViewHolderFactory
import apptentive.com.android.feedback.survey.view.SurveySegmentedProgressBar
import apptentive.com.android.feedback.survey.viewmodel.MultiChoiceQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.RangeQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SingleLineQuestionListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyFooterListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyHeaderListItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyIntroductionPageItem
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.Footer
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.Header
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.Introduction
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.RangeQuestion
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.SingleLineQuestion
import apptentive.com.android.feedback.survey.viewmodel.SurveyListItem.Type.Success
import apptentive.com.android.feedback.survey.viewmodel.SurveySuccessPageItem
import apptentive.com.android.feedback.survey.viewmodel.register
import apptentive.com.android.ui.ApptentiveGenericDialog
import apptentive.com.android.ui.ApptentivePagerAdapter
import apptentive.com.android.ui.LayoutViewHolderFactory
import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.hideSoftKeyboard
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.SURVEY
import com.google.android.material.appbar.MaterialToolbar
import apptentive.com.android.R.string.apptentive_cancel
import apptentive.com.android.R.string.apptentive_close
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView

internal class SurveyActivity : BaseSurveyActivity() {

    private var confirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_survey)

        onBackPressedDispatcher.addCallback {
            viewModel.exit(showConfirmation = true)
        }

        try {
            title = viewModel.title // So TalkBack announces the survey title

            supportActionBar?.hide()

            val topAppBar = findViewById<MaterialToolbar>(R.id.apptentive_top_app_bar)
            topAppBar.setNavigationOnClickListener {
                it.hideSoftKeyboard()
                viewModel.exit(showConfirmation = true)
            }

            val topAppBarTitle = findViewById<MaterialTextView>(R.id.apptentive_survey_title)
            topAppBarTitle.text = viewModel.title

            if (viewModel.isPaged) setupPagedSurvey() else setupListSurvey()

            val bottomAppBar = findViewById<LinearLayout>(R.id.apptentive_bottom_app_bar)
            bottomAppBar.importantForAccessibility =
                if (viewModel.termsAndConditions.isNullOrEmpty()) View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                else View.IMPORTANT_FOR_ACCESSIBILITY_YES

            val termsAndConditionsText = findViewById<MaterialTextView>(R.id.apptentive_terms_and_conditions)
            termsAndConditionsText.movementMethod = LinkMovementMethod.getInstance()
            termsAndConditionsText.text = viewModel.termsAndConditions

            viewModel.exitStream.observe(this) {
                finish()
            }

            viewModel.showConfirmation.observe(this) {
                if (it) {
                    with(viewModel.surveyCancelConfirmationDisplay) {
                        confirmationDialog = ApptentiveGenericDialog().getGenericDialog(
                            context = this@SurveyActivity,
                            title = title ?: getString(R.string.confirmation_dialog_title),
                            message = message ?: getString(R.string.confirmation_dialog_message),
                            positiveButton = ApptentiveGenericDialog.DialogButton(positiveButtonMessage ?: getString(apptentive_cancel)) {
                                viewModel.onBackToSurveyFromConfirmationDialog()
                            },
                            negativeButton = ApptentiveGenericDialog.DialogButton(negativeButtonMessage ?: getString(apptentive_close)) {
                                viewModel.exit(showConfirmation = false)
                            }
                        )

                        confirmationDialog?.show()
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e(SURVEY, "Error launching survey activity $exception")
            finish()
        }
    }

    private fun setupListSurvey() {
        val listAdapter = createListAdapter()
        val listRecyclerView = findViewById<RecyclerView>(R.id.apptentive_list_survey_recycler_view)
        listRecyclerView.adapter = listAdapter
        listRecyclerView.isVisible = true

        viewModel.listItems.observe(this) { items ->
            listAdapter.submitList(items)
        }

        viewModel.firstInvalidQuestionIndex.observe(this) { firstErrorPosition ->
            if (firstErrorPosition != -1) {

                // Check if item is fully visible on screen before trying to scroll
                val layoutManger = (listRecyclerView.layoutManager as LinearLayoutManager)
                if (firstErrorPosition !in layoutManger.findFirstCompletelyVisibleItemPosition()..layoutManger.findLastCompletelyVisibleItemPosition()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        listRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                                if (newState == SCROLL_STATE_IDLE) {
                                    val errorView = layoutManger.findViewByPosition(firstErrorPosition)
                                    errorView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                                    recyclerView.removeOnScrollListener(this)
                                }
                            }
                        })
                    }
                    listRecyclerView.smoothScrollToPosition(firstErrorPosition)
                } else {
                    val errorView = layoutManger.findViewByPosition(firstErrorPosition)
                    errorView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }
        }
    }

    private fun setupPagedSurvey() {
        val surveyPager = findViewById<ViewPager2>(R.id.apptentive_survey_view_pager)
        val pagedAdapter = createPagedAdapter()

        surveyPager.setPageTransformer { _, _ -> } // Disable update animation when error shows
        surveyPager.isUserInputEnabled = false // Disable swipe to change page
        surveyPager.adapter = pagedAdapter

        // For Talkback: Sets accessibility focus to the question after page change
        surveyPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    val pagerRecycler = surveyPager.getChildAt(surveyPager.childCount - 1) as? RecyclerView
                    pagerRecycler?.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                    pagerRecycler?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
                    pagerRecycler?.requestFocus()
                }
            }
        })

        viewModel.currentPage.observe(this) { page ->
            pagedAdapter.addOrUpdatePage(page, currentFocus is EditText)
            surveyPager.setCurrentItem(pagedAdapter.itemCount - 1, true)
        }

        setupNextButton()
        setupProgressBar()

        val pagedLayout = findViewById<LinearLayout>(R.id.apptentive_paged_survey_layout)
        pagedLayout.isVisible = true
    }

    private fun setupNextButton() {
        val nextButton = findViewById<MaterialButton>(R.id.apptentive_next_button)

        viewModel.advanceButtonText.observe(this) { advanceText ->
            nextButton.text = advanceText
        }

        nextButton.setOnClickListener {
            it.hideSoftKeyboard()
            viewModel.advancePage()
        }
    }

    private fun setupProgressBar() {
        val useSegmentedProgressBar = viewModel.pageCount in 2..10

        if (useSegmentedProgressBar) {
            val segmentedProgressBar = findViewById<SurveySegmentedProgressBar>(R.id.apptentive_progress_bar_segmented)
            segmentedProgressBar.isVisible = true
            segmentedProgressBar.setSegmentCount(viewModel.pageCount)
            viewModel.progressBarNumber.observe(this) { progressNumber ->
                if (progressNumber != null) segmentedProgressBar.updateProgress(progressNumber)
                else segmentedProgressBar.visibility = View.INVISIBLE
            }
        } else {
            val linearProgressBar = findViewById<LinearProgressIndicator>(R.id.apptentive_progress_bar_linear)
            linearProgressBar.isVisible = true
            viewModel.progressBarNumber.observe(this) { progressNumber ->
                if (progressNumber != null) linearProgressBar.setProgressCompat(
                    (progressNumber + 1) * 100 / viewModel.pageCount,
                    true
                )
                else linearProgressBar.visibility = View.INVISIBLE
            }
        }
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
        if (!viewModel.isPaged && event.action == MotionEvent.ACTION_DOWN) {
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

    private fun createListAdapter() = ListViewAdapter().apply {
        register(
            type = Header,
            factory = LayoutViewHolderFactory(R.layout.apptentive_survey_header) {
                SurveyHeaderListItem.ViewHolder(it)
            }
        )

        register(
            type = SingleLineQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_singleline, false) {
                SingleLineQuestionListItem.ViewHolder(it) { questionId, text ->
                    viewModel.updateAnswer(questionId, text)
                }
            }
        )

        register(
            type = RangeQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_range, false) {
                RangeQuestionListItem.ViewHolder(it) { questionId, selectedIndex ->
                    viewModel.updateAnswer(questionId, selectedIndex)
                }
            }
        )

        register(
            type = MultiChoiceQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_multichoice, false) {
                MultiChoiceQuestionListItem.ViewHolder(it) { questionId, choiceId, selected, text ->
                    viewModel.updateAnswer(questionId, choiceId, selected, text)
                }
            }
        )

        register(
            type = Footer,
            factory = LayoutViewHolderFactory(R.layout.apptentive_survey_footer) {
                SurveyFooterListItem.ViewHolder(it) {
                    viewModel.submitListSurvey()
                }
            }
        )
    }

    private fun createPagedAdapter() = ApptentivePagerAdapter().apply {
        register(
            type = Introduction,
            factory = LayoutViewHolderFactory(R.layout.apptentive_survey_introduction) {
                SurveyIntroductionPageItem.ViewHolder(it)
            }
        )

        register(
            type = SingleLineQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_singleline, true) {
                SingleLineQuestionListItem.ViewHolder(it, true) { questionId, text ->
                    viewModel.updateAnswer(questionId, text)
                }
            }
        )

        register(
            type = RangeQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_range, true) {
                RangeQuestionListItem.ViewHolder(it) { questionId, selectedIndex ->
                    viewModel.updateAnswer(questionId, selectedIndex)
                }
            }
        )

        register(
            type = MultiChoiceQuestion,
            factory = SurveyQuestionViewHolderFactory(R.layout.apptentive_survey_question_multichoice, true) {
                MultiChoiceQuestionListItem.ViewHolder(it) { questionId, choiceId, selected, text ->
                    viewModel.updateAnswer(questionId, choiceId, selected, text)
                }
            }
        )

        register(
            type = Success,
            factory = LayoutViewHolderFactory(R.layout.apptentive_survey_success_page) {
                SurveySuccessPageItem.ViewHolder(it)
            }
        )
    }
}
