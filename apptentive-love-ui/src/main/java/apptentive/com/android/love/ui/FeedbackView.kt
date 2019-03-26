package apptentive.com.android.love.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import apptentive.com.android.love.SentimentType
import kotlinx.android.synthetic.main.feedback_view.view.*

class FeedbackView(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val buttons = listOf(button_satisfied, button_neutral, button_dissatisfied)

    init {
        View.inflate(context, R.layout.feedback_view, this)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FeedbackView)
            val title = typedArray.getString(R.styleable.FeedbackView_title)
            if (title != null) {
                title_view.text = title
            }
            typedArray.recycle()
        }

        setButtonListener(button_satisfied, SentimentType.POSITIVE)
        setButtonListener(button_neutral, SentimentType.NEUTRAL)
        setButtonListener(button_dissatisfied, SentimentType.NEGATIVE)
    }

    private fun setButtonListener(button: ImageButton, sentimentType: SentimentType) {
        button.setOnClickListener {
            buttons.forEach { it.isSelected = it == button }
            sendSentiment(sentimentType)
        }
    }

    private fun sendSentiment(sentimentType: SentimentType) {
        print(sentimentType)
    }
}