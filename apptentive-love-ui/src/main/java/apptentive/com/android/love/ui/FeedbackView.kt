package apptentive.com.android.love.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.love.LoveSender
import apptentive.com.android.love.Sentiment
import apptentive.com.android.love.SentimentType
import kotlinx.android.synthetic.main.feedback_view.view.*

class FeedbackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val buttons: List<ImageButton>

    var sentimentIdentifier: String? = null

    init {
        View.inflate(context, R.layout.feedback_view, this)

        buttons = listOf(button_positive, button_neutral, button_negative)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FeedbackView)
            val sentimentIdentifier = typedArray.getString(R.styleable.FeedbackView_sentiment)
            if (sentimentIdentifier == null || sentimentIdentifier.isEmpty()) {
                throw IllegalStateException("Missing feedback view sentiment identifier")
            }

            this.sentimentIdentifier = sentimentIdentifier

            val title = typedArray.getString(R.styleable.FeedbackView_title)
            if (title != null) {
                title_view.text = title
            }
            typedArray.recycle()
        }

        setButtonListener(button_positive, SentimentType.POSITIVE)
        setButtonListener(button_neutral, SentimentType.NEUTRAL)
        setButtonListener(button_negative, SentimentType.NEGATIVE)
    }

    private fun setButtonListener(button: ImageButton, sentimentType: SentimentType) {
        button.setOnClickListener {
            buttons.forEach { it.isSelected = it == button }
            sendSentiment(sentimentType)
        }
    }

    private fun sendSentiment(sentimentType: SentimentType) {
        val sentiment = Sentiment(sentimentIdentifier!!, sentimentType)
        val sender = DependencyProvider.of<LoveSender>()
        sender.send(sentiment)
    }
}