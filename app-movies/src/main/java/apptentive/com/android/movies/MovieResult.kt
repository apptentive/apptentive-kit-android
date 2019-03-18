package apptentive.com.android.movies

import com.google.gson.annotations.SerializedName
import java.lang.IllegalStateException

data class MovieResult(@SerializedName("results") private val _movies: Array<Movie>? = null) {
    val movies: Array<Movie> get() = _movies ?: throw IllegalStateException("Movies must not be null")
}