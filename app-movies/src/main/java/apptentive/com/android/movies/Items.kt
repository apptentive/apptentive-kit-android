package apptentive.com.android.movies

import android.view.View
import android.widget.ImageView
import apptentive.com.android.movies.util.Item
import apptentive.com.android.movies.util.RecyclerViewAdapter

enum class ItemType {
    MOVIE,
    FEEDBACK,
    RATING,
    SURVEY
}

internal interface ImageLoader {
    fun loadImage(path: String, imageView: ImageView)
}

internal data class MovieItem(val movie: Movie) : Item(ItemType.MOVIE.ordinal) {
    class ViewHolder(convertView: View, private val imageLoader: ImageLoader) : RecyclerViewAdapter.ViewHolder<MovieItem>(convertView) {
        private val imageView: ImageView = convertView.findViewById(R.id.imageView)

        override fun bindView(item: MovieItem, position: Int) {
            imageLoader.loadImage(item.movie.posterPath, imageView)
        }
    }
}

internal data class FeedbackItem(val movie: Movie) : Item(ItemType.FEEDBACK.ordinal)
internal data class RatingItem(val movie: Movie) : Item(ItemType.RATING.ordinal)
internal data class SurveyItem(val movie: Movie) : Item(ItemType.SURVEY.ordinal)