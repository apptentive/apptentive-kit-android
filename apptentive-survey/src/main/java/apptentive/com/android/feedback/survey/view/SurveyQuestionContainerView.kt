package apptentive.com.android.feedback.survey.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.ui.getThemeColor

class SurveyQuestionContainerView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {
    private val titleTextView: TextView
    private val instructionsTextView: TextView
    private val answerContainerView: ViewGroup // FIXME: change to ViewStub https://developer.android.com/training/improving-layouts/loading-ondemand
    private val errorMessageView: TextView

    var title: CharSequence?
        get() = titleTextView.text
        set(value) {
            titleTextView.text = value
        }

    var instructions: CharSequence?
        get() = instructionsTextView.text
        set(value) {
            // hide instructions if there's no value
            instructionsTextView.isVisible = !value.isNullOrEmpty()
            instructionsTextView.text = value
        }

    // hold view initial text colors to restore later
    private val titleTextViewDefaultColor: ColorStateList
    private val instructionsTextViewDefaultColor: ColorStateList

    // error "override" color
    private val errorColor: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val contentView = LayoutInflater.from(context)
            .inflate(R.layout.apptentive_survey_question_container, this, true)

        titleTextView = contentView.findViewById(R.id.question_title)
        instructionsTextView = contentView.findViewById(R.id.question_instructions)
        answerContainerView = contentView.findViewById(R.id.answer_container)
        errorMessageView = contentView.findViewById(R.id.error_message)

        titleTextViewDefaultColor = titleTextView.textColors
        instructionsTextViewDefaultColor = instructionsTextView.textColors

        errorColor = context.getThemeColor(R.attr.colorError)
    }

    fun setAnswerView(layoutId: Int) {
        answerContainerView.removeAllViews()
        val answerView = LayoutInflater.from(context).inflate(layoutId, answerContainerView, false)
        answerContainerView.addView(answerView)
    }

    fun setErrorMessage(errorMessage: String?) {
        if (errorMessage != null) {
            titleTextView.setTextColor(errorColor)
            instructionsTextView.setTextColor(errorColor)
            errorMessageView.visibility = View.VISIBLE
            errorMessageView.text = errorMessage
        } else {
            titleTextView.setTextColor(titleTextViewDefaultColor)
            instructionsTextView.setTextColor(instructionsTextViewDefaultColor)
            errorMessageView.visibility = View.INVISIBLE
        }
    }
}