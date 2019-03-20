package apptentive.com.android.movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MovieDetailViewModel(
    application: Application,
    private val repository: MovieRepository
) : AndroidViewModel(application) {
}