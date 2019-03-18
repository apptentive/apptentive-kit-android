package apptentive.com.android.movies

import android.app.Application

class MovieApplication: Application() {
    private lateinit var repository: MovieRepository

    override fun onCreate() {
        super.onCreate()
    }
}