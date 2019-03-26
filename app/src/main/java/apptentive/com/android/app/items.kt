package apptentive.com.android.app

import android.view.View
import android.widget.Button
import android.widget.ImageButton
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
        private val buttons = arrayOf<ImageButton>(
            view.findViewById(R.id.button_satisfied),
            view.findViewById(R.id.button_neutral),
            view.findViewById(R.id.button_dissatisfied)
        )
        private val colors = intArrayOf(
            R.color.colorSatisfied,
            R.color.colorNeutral,
            R.color.colorDissatisfied)

        init {
            val colorFilter = buttons[0].colorFilter
            for (i in 0 until buttons.size) {
                buttons[i].setOnClickListener {
                    for (j in 0 until buttons.size) {
                        if (i == j) {
                            buttons[j].setColorFilter(buttons[j].context.getColor(colors[j]))
                        } else {
                            buttons[j].colorFilter = colorFilter
                        }
                    }
                }
            }
        }

        override fun bindView(item: FeedbackItem, position: Int) {
            titleView.text = item.title
        }
    }
}

class RatingItem(private val title: String) : Item(ItemType.RATING.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<RatingItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.title)
        private val buttons = arrayOf<ImageButton>(
            view.findViewById(R.id.button_star_1),
            view.findViewById(R.id.button_star_2),
            view.findViewById(R.id.button_star_3),
            view.findViewById(R.id.button_star_4),
            view.findViewById(R.id.button_star_5)
        )

        init {
            for (i in 0 until buttons.size) {
                buttons[i].setOnClickListener { setRating(i + 1) }
            }
        }

        override fun bindView(item: RatingItem, position: Int) {
            titleView.text = item.title
        }

        private fun setRating(rating: Int) {
            for (i in 0 until buttons.size) {
                val selected = i < rating
                buttons[i].setImageResource(if (selected) R.drawable.ic_star_black else R.drawable.ic_star_border)
            }
        }
    }
}

class SurveyItem(private val question: String, private val answers: Array<String>) : Item(ItemType.SURVEY.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<SurveyItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.question)
        private val buttons = arrayOf<Button>(
            view.findViewById(R.id.button_answer_1),
            view.findViewById(R.id.button_answer_2),
            view.findViewById(R.id.button_answer_3)
        )

        init {
            val colors = buttons[0].textColors
            for (i in 0 until buttons.size) {
                buttons[i].setOnClickListener {
                    for (j in 0 until buttons.size) {
                        if (i == j) {
                            buttons[j].setTextColor(buttons[j].context.getColor(R.color.colorAccent))
                        } else {
                            buttons[j].setTextColor(colors)
                        }
                    }
                }
            }
        }

        override fun bindView(item: SurveyItem, position: Int) {
            titleView.text = item.question
            for (i in 0 until item.answers.size) {
                buttons[i].text = item.answers[i]
            }
        }
    }
}