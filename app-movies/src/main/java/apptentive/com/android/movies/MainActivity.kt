package apptentive.com.android.movies

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import apptentive.com.android.movies.util.Item
import apptentive.com.android.movies.util.RecyclerViewAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.mainToolbar))

        val adapter = createAdapter()
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter

        val viewModel = getViewModel()
        viewModel.movies.observe(this, Observer { movies ->
            val items: MutableList<Item> = movies.map { MovieItem(it) }.toMutableList()
            items.add(RatingItem("How would your rate the app?"))
            items.add(16, FeedbackItem("How was your last renting experience?"))
            items.add(8, SurveyItem("Local or Foreign?", arrayOf("Local", "Foreign")))
            items.add(4, SurveyItem("What's your favourite genre?", arrayOf("Comedy", "Horror", "Drama")))
            adapter.setItems(items)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (items[position] is MovieItem) 1 else 2
                }
            }
        })
    }

    private fun getViewModel(): MovieViewModel {
        val factory = MovieViewModelFactory.getInstance(this) // FIXME: replace with DI
        return ViewModelProviders.of(this, factory).get(MovieViewModel::class.java)
    }

    private fun createAdapter(): RecyclerViewAdapter {
        val imageLoader = AssetImageLoader()
        val adapter = RecyclerViewAdapter()
        val movieClickListener = object : MovieItem.MovieItemClickListener {
            override fun onMovieItemClicked(movieItem: MovieItem) {
                openMovieDetails(movieItem.movie)
            }
        }

        adapter.register(ItemType.MOVIE, object : RecyclerViewAdapter.LayoutIdFactory<MovieItem>(R.layout.movie_item) {
            override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<MovieItem> {
                return MovieItem.ViewHolder(convertView, imageLoader, movieClickListener)
            }
        })
        adapter.register(
            ItemType.FEEDBACK,
            object : RecyclerViewAdapter.LayoutIdFactory<FeedbackItem>(R.layout.feedback_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<FeedbackItem> {
                    return FeedbackItem.ViewHolder(convertView)
                }
            })
        adapter.register(
            ItemType.RATING,
            object : RecyclerViewAdapter.LayoutIdFactory<RatingItem>(R.layout.rating_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<RatingItem> {
                    return RatingItem.ViewHolder(convertView)
                }
            })
        adapter.register(
            ItemType.SURVEY,
            object : RecyclerViewAdapter.LayoutIdFactory<SurveyItem>(R.layout.survey_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<SurveyItem> {
                    return SurveyItem.ViewHolder(convertView)
                }
            })

        return adapter
    }

    private fun openMovieDetails(movie: Movie) {

    }
}

private class AssetImageLoader : ImageLoader {
    override fun loadImage(path: String, imageView: ImageView) {
        val imageFile = "file:///android_asset/$path"
        Picasso.get().load(imageFile).into(imageView)
    }
}