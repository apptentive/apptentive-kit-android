package apptentive.com.android.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MovieViewModel(
    repository: MovieRepository
) : ViewModel() {
    val movies: LiveData<Array<Movie>> = repository.movies
}
