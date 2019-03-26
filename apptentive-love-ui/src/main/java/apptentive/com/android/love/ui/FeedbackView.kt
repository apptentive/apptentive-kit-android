package apptentive.com.android.love.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

class FeedbackView(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.feedback_view, this)
    }
}