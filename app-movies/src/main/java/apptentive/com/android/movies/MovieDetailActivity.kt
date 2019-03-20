package apptentive.com.android.movies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MovieDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)
    }

    companion object {
        val EXTRA_MOVIE_ID = "com.apptentive.MOVIE_ID"
    }
}
