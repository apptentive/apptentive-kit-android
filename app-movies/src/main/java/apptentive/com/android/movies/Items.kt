package apptentive.com.android.movies

import android.view.View
import android.widget.*
import apptentive.com.android.love.*
import apptentive.com.android.love.ui.RatingView
import apptentive.com.android.movies.util.Item
import apptentive.com.android.movies.util.RecyclerViewAdapter

enum class ItemType {
    MOVIE,
    FEEDBACK,
    RATING,
    SURVEY
}

internal data class MovieItem(val movie: Movie) : Item(ItemType.MOVIE.ordinal) {
    class ViewHolder(
        convertView: View,
        private val imageLoader: ImageLoader,
        private val clickListener: MovieItemClickListener
    ) : RecyclerViewAdapter.ViewHolder<MovieItem>(convertView) {
        private val movieButton: ImageButton = convertView.findViewById(R.id.movieButton)

        override fun bindView(item: MovieItem, position: Int) {
            imageLoader.loadImage(item.movie.posterPath, movieButton)
            movieButton.setOnClickListener {
                clickListener.onMovieItemClicked(item)
            }
        }
    }

    interface MovieItemClickListener {
        fun onMovieItemClicked(movieItem: MovieItem)
    }
}

abstract class AbstractLoveItem(itemType: Int, val identifier: String) : Item(itemType)

class FeedbackItem(identifier: String, private val title: String) :
    AbstractLoveItem(ItemType.FEEDBACK.ordinal, identifier) {
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
            R.color.colorDissatisfied
        )
        private val sentiments = arrayOf(
            SentimentType.POSITIVE,
            SentimentType.NEUTRAL,
            SentimentType.NEGATIVE
        )

        private lateinit var identifier: String

        init {
            val colorFilter = buttons[0].colorFilter
            for (i in 0 until buttons.size) {
                buttons[i].setOnClickListener {
                    for (j in 0 until buttons.size) {
                        if (i == j) {
                            buttons[j].setColorFilter(buttons[j].context.getColor(colors[j]))
                            ApptentiveLove.send(Sentiment(identifier, sentiments[j]))
                        } else {
                            buttons[j].colorFilter = colorFilter
                        }
                    }
                }
            }
        }

        override fun bindView(item: FeedbackItem, position: Int) {
            identifier = item.identifier
            titleView.text = item.title
        }
    }
}

class RatingItem(identifier: String, private val title: String) :
    AbstractLoveItem(ItemType.RATING.ordinal, identifier) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<RatingItem>(view) {
        private val ratingView: RatingView = view.findViewById(R.id.rating_view)

        override fun bindView(item: RatingItem, position: Int) {
            ratingView.title = item.title
            ratingView.loveIdentifier = item.identifier
        }
    }
}

data class SurveyAnswer(val identifier: String, val title: String)

class SurveyItem(identifier: String, private val question: String, private val answers: Array<SurveyAnswer>) :
    AbstractLoveItem(ItemType.SURVEY.ordinal, identifier) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<SurveyItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.question)
        private val buttons = arrayOf<Button>(
            view.findViewById(R.id.button_answer_1),
            view.findViewById(R.id.button_answer_2),
            view.findViewById(R.id.button_answer_3)
        )
        private lateinit var answers: List<String>

        init {
            val colors = buttons[0].textColors
            for (i in 0 until buttons.size) {
                buttons[i].setOnClickListener {
                    for (j in 0 until buttons.size) {
                        if (i == j) {
                            buttons[j].setTextColor(buttons[j].context.getColor(R.color.colorAccent))
                            ApptentiveLove.send(SurveyResponse(answers[j]))
                        } else {
                            buttons[j].setTextColor(colors)
                        }
                    }
                }
            }
        }

        override fun bindView(item: SurveyItem, position: Int) {
            answers = item.answers.map { it.identifier }

            titleView.text = item.question
            for (i in 0 until item.answers.size) {
                buttons[i].text = item.answers[i].title
                buttons[i].visibility = View.VISIBLE
            }
            for (i in item.answers.size until buttons.size) {
                buttons[i].visibility = View.GONE
            }
        }
    }
}