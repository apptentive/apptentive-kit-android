package apptentive.com.android.movies

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import apptentive.com.android.concurrent.ExecutorQueue

class MovieDetailViewModel(
    private val repository: MovieRepository,
    private val executor: ExecutorQueue
) : ViewModel() {
    fun findMovie(id: String): Movie? {
        return repository.findMovie(id)
    }

    fun rentMovie(movie: Movie, callback: MovieRentCallback? = null) {
        executor.execute(3.0) {
            callback?.onRentComplete(movie)
        }
    }

    fun showRentConfirmation(context: Context, movie: Movie) {
        val intent = Intent(context, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_MOVIE_ID, movie.id)
        context.startActivity(intent)
    }

    interface MovieRentCallback {
        fun onRentComplete(movie: Movie)
    }
}