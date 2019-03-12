package apptentive.com.android.app

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

enum class ItemType {
    BEVERAGE,
    FEEDBACK,
    RATING,
    SURVEY
}

class BeverageItem(val id: String, val imageId: Int, val title: String) : Item(ItemType.BEVERAGE.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<BeverageItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.item_title)
        private val imageView: ImageView = view.findViewById(R.id.item_icon)

        override fun bindView(item: BeverageItem, position: Int) {
            titleView.text = item.title
            imageView.setImageResource(item.imageId)
        }
    }
}

class FeedbackItem(private val title: String) : Item(ItemType.FEEDBACK.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<FeedbackItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.title)

        override fun bindView(item: FeedbackItem, position: Int) {
            titleView.text = item.title
        }
    }
}

class RatingItem(private val title: String) : Item(ItemType.RATING.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<RatingItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.title)

        override fun bindView(item: RatingItem, position: Int) {
            titleView.text = item.title
        }
    }
}

class SurveyItem(private val question: String, private val answers: Array<String>) : Item(ItemType.SURVEY.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<SurveyItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.question)
        private val answerButtons = arrayOf<Button>(
            view.findViewById(R.id.button_answer_1),
            view.findViewById(R.id.button_answer_2),
            view.findViewById(R.id.button_answer_3)
        )

        override fun bindView(item: SurveyItem, position: Int) {
            titleView.text = item.question
            for (i in 0 until item.answers.size) {
                answerButtons[i].text = item.answers[i]
            }
        }
    }
}