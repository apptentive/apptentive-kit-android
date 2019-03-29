package apptentive.com.android.movies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_movie_detail.*

class MovieDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val movieId = intent.extras[EXTRA_MOVIE_ID] as String

        val factory = ViewModelFactory.getInstance(this)
        val viewModel = ViewModelProviders.of(this, factory).get(MovieDetailViewModel::class.java)
        val movie = viewModel.findMovie(movieId)!!

        // action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // backdrop
        val imageLoader = AssetImageLoader()
        imageLoader.loadImage(movie.backdropPath, backdropImageView)

        // title
        movieTitle.text = movie.title

        // overview
        movieOverview.text = movie.overview

    }

    companion object {
        const val EXTRA_MOVIE_ID = "com.apptentive.MOVIE_ID"
    }
}
