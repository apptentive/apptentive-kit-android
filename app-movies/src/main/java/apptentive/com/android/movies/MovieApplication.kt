package apptentive.com.android.movies

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MovieApplication : Application() {
    private lateinit var repository: MovieRepository

    override fun onCreate() {
        super.onCreate()
        repository = MockMovieRepository(this)
    }
}

class MovieViewModelFactory(
    private val application: Application,
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            return MovieViewModel(application, repository) as T
        }

        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            return MovieDetailViewModel(application, repository) as T
        }

        throw IllegalArgumentException("Unknown model class: $modelClass")
    }
}

