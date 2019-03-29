package apptentive.com.android.movies

import androidx.lifecycle.ViewModel

class MovieDetailViewModel(
    private val repository: MovieRepository
) : ViewModel() {
    fun findMovie(id: String): Movie? {
        return repository.findMovie(id)
    }
}