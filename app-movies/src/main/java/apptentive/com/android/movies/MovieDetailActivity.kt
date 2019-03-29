package apptentive.com.android.movies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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

        // rent button
        movieRentButton.setOnClickListener {
            val dialog = createProgressDialog("Processing...")
            dialog.show()
            viewModel.rentMovie(movie, object : MovieDetailViewModel.MovieRentCallback {
                override fun onRentComplete(movie: Movie) {
                    dialog.dismiss()
                    viewModel.showRentConfirmation(movie)
                    finish()
                }
            })
        }
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
