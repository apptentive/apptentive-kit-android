package apptentive.com.android.movies

import com.google.gson.annotations.SerializedName

// TODO: use Moshi's Kotlin Code Gen
data class Movie(
    @SerializedName("id") private val _id: String? = null,
    @SerializedName("title") private val _title: String? = null,
    @SerializedName("poster_path") private val _posterPath: String? = null,
    @SerializedName("backdrop_path") private val _backdropPath: String? = null,
    @SerializedName("overview") private val _overview: String? = null,
    @SerializedName("release_date") private val _release_date: String? = null
) {

    val id get () = _id ?: throw java.lang.IllegalStateException("Id should not be null")
    val title get() = _title ?: throw IllegalStateException("Title should not be null")
    val posterPath get() = _posterPath ?: throw IllegalStateException("Poster path should not be null")
    val backdropPath get() = _backdropPath ?: throw IllegalStateException("Backdrop path should not be null")
    val overview get() = _overview ?: throw IllegalStateException("Overview should not be null")
    val releaseDate get() = _release_date ?: throw IllegalStateException("Release date should not be null")
    var favourite: Boolean = false
}
