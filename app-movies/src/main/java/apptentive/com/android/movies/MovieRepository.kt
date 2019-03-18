package apptentive.com.android.movies

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson

class MovieRepository(context: Context) {
    val movies: MutableLiveData<Array<Movie>> = MutableLiveData()

    init {
        movies.value = fetchMovies(context)
    }

    private fun fetchMovies(context: Context): Array<Movie> {
        context.assets.open("top_rated.json").bufferedReader().use { stream ->
            val json = stream.readText()
            val movieResult = Gson().fromJson(json, MovieResult::class.java)
            return movieResult.movies
        }
    }
}