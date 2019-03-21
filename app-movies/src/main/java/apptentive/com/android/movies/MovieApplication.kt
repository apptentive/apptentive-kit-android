package apptentive.com.android.movies

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import apptentive.com.android.love.ApptentiveLove

class MovieApplication : Application() {
    private lateinit var repository: MovieRepository

    override fun onCreate() {
        super.onCreate()
        repository = MockMovieRepository(this)

        ApptentiveLove.register(this, "app-key", "app-signature")
    }
}

class MovieViewModelFactory private constructor(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            return MovieViewModel(repository) as T
        }

        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            return MovieDetailViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown model class: $modelClass")
    }

    companion object {
        private var instance: MovieViewModelFactory? = null

        @Synchronized fun getInstance(context: Context) : MovieViewModelFactory {
            if (instance == null) {
                val repository = MockMovieRepository(context)
                instance = MovieViewModelFactory(repository)
            }
            return instance!!
        }
    }
}

