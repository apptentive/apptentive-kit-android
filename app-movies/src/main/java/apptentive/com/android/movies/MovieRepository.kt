package apptentive.com.android.movies

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson

interface MovieRepository {
    val movies: LiveData<Array<Movie>>
    fun findMovie(id: String): Movie?
}

class MockMovieRepository(context: Context) : MovieRepository {
    override val movies: MutableLiveData<Array<Movie>> = MutableLiveData()

    init {
        movies.value = fetchMovies(context)
    }

    override fun findMovie(id: String): Movie? {
        return movies.value?.find { id == it.id }
    }

    private fun fetchMovies(context: Context): Array<Movie> {
        context.assets.open("top_rated.json").bufferedReader().use { stream ->
            val json = stream.readText()
            val movieResult = Gson().fromJson(json, MovieResult::class.java)
            return movieResult.movies
        }
    }
}