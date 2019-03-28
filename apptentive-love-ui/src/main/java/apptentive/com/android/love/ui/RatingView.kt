package apptentive.com.android.love.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RatingBar
import apptentive.com.android.love.Rating
import kotlinx.android.synthetic.main.rating_view.view.*

class RatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AbstractLoveView(context, attrs) {

    var title: String
        get() {
            return title_view.text.toString()
        }
        set(value) {
            title_view.text = value
        }

    init {
        View.inflate(context, R.layout.rating_view, this)

        rating_bar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                sendLoveEntity(Rating(loveIdentifier!!, rating))
            }

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AbstractLoveView)
            val title = typedArray.getString(R.styleable.AbstractLoveView_title)
            if (title != null) {
                title_view.text = title
            }
            typedArray.recycle()
        }
    }
}