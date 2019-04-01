package apptentive.com.android.movies

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.love.ApptentiveLove

class ViewModelFactory private constructor(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            return MovieViewModel(repository) as T
        }

        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            return MovieDetailViewModel(repository, ExecutorQueue.mainQueue) as T
        }

        if (modelClass.isAssignableFrom(ConfirmationViewModel::class.java)) {
            return ConfirmationViewModel(repository) as T
        }

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(ApptentiveLove.person) as T
        }

        throw IllegalArgumentException("Unknown model class: $modelClass")
    }

    companion object {
        private var instance: ViewModelFactory? = null

        @Synchronized fun getInstance(context: Context) : ViewModelFactory {
            if (instance == null) {
                val repository = MockMovieRepository(context)
                instance = ViewModelFactory(repository)
            }
            return instance!!
        }
    }
}