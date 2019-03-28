package apptentive.com.android.love.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import apptentive.com.android.love.Sentiment
import apptentive.com.android.love.SentimentType
import kotlinx.android.synthetic.main.feedback_view.view.*

class FeedbackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AbstractLoveView(context, attrs) {
    private val buttons: List<ImageButton>

    var title: String
        get() {
            return title_view.text.toString()
        }
        set(value) {
            title_view.text = value
        }

    init {
        View.inflate(context, R.layout.feedback_view, this)

        buttons = listOf(button_positive, button_neutral, button_negative)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FeedbackView)
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
        sendLoveEntity(sentiment)
    }
}