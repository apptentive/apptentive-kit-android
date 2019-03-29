package apptentive.com.android.movies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import apptentive.com.android.love.ApptentiveLove
import apptentive.com.android.love.Sentiment
import apptentive.com.android.love.SentimentType
import kotlinx.android.synthetic.main.activity_movie_detail.*

class MovieDetailActivity : AppCompatActivity() {
    private lateinit var movie: Movie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val movieId = intent.extras[EXTRA_MOVIE_ID] as String

        val factory = ViewModelFactory.getInstance(this)
        val viewModel = ViewModelProviders.of(this, factory).get(MovieDetailViewModel::class.java)
        movie = viewModel.findMovie(movieId)!!

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

        // rent button
        movieRentButton.setOnClickListener {
            val dialog = createProgressDialog("Processing...")
            dialog.show()
            viewModel.rentMovie(movie, object : MovieDetailViewModel.MovieRentCallback {
                override fun onRentComplete(movie: Movie) {
                    dialog.dismiss()
                    viewModel.showRentConfirmation(this@MovieDetailActivity, movie)
                    finish()
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_movie_details, menu)
        val favouriteItem = menu.findItem(R.id.buttonFavourite)
        favouriteItem.setIcon(if (movie.favourite) R.drawable.ic_favorite else R.drawable.ic_favorite_empty)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.buttonFavourite) {
            if (!movie.favourite) {
                movie.favourite = true
                item.setIcon(R.drawable.ic_favorite)

                ApptentiveLove.send(Sentiment("movie_${movie.id}", SentimentType.POSITIVE))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun createProgressDialog(message: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(true)
            .create()
    }

    companion object {
        const val EXTRA_MOVIE_ID = "com.apptentive.MOVIE_ID"
    }
}
