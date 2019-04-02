package apptentive.com.android.movies

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import apptentive.com.android.love.ApptentiveLove
import apptentive.com.android.movies.util.Item
import apptentive.com.android.movies.util.RecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        setSupportActionBar(mainToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        // Navigation
        navigationView.setNavigationItemSelectedListener { item ->
            drawer.closeDrawers()

            if (item.itemId == R.id.nav_profile) {
                showProfile()
                true
            }
            if (item.itemId == R.id.nav_statistics) {
                showStatistics()
                true
            }

            false
        }

        val adapter = createAdapter()
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter

        val viewModel = getViewModel()
        viewModel.movies.observe(this, Observer { movies ->
            val items: MutableList<Item> = movies.map { MovieItem(it) }.toMutableList()
            items.add(RatingItem("rating_app", "How would your rate the app?"))
            items.add(16, FeedbackItem("feedback_renting_experience", "How was your last renting experience?"))
            items.add(
                8, SurveyItem(
                    "survey_origin", "Local or Foreign?",
                    arrayOf(
                        SurveyAnswer("survey_origin_local", "Local"),
                        SurveyAnswer("survey_origin_foreigns", "Foreign")
                    )
                )
            )
            items.add(
                4,
                SurveyItem(
                    "survey_genre",
                    "What's your favourite genre?",
                    arrayOf(
                        SurveyAnswer("survey_genre_comedy", "Comedy"),
                        SurveyAnswer("survey_genre_horror", "Horror"),
                        SurveyAnswer("survey_genre_drama", "Drama")
                    )
                )
            )
            adapter.setItems(items)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (items[position] is MovieItem) 1 else 2
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val header = navigationView.getHeaderView(0)

        val username = ApptentiveLove.person.name
        val usernameView: TextView = header.findViewById(R.id.username)
        usernameView.text = if (username.isNullOrEmpty()) getString(R.string.user_anonymous) else username

        val email = ApptentiveLove.person.email
        val emailView: TextView = header.findViewById(R.id.email)
        if (!email.isNullOrEmpty()) {
            emailView.visibility = View.VISIBLE
            emailView.text = email
        } else {
            emailView.visibility = View.GONE
        }

        val profileView: ImageView = header.findViewById(R.id.profileImage)
        if (email == "melody@apptentive.com") {
            profileView.setImageDrawable(getDrawable(R.drawable.mel))
        } else {
            profileView.setImageDrawable(getDrawable(R.drawable.ic_face_black_48dp))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getViewModel(): MovieViewModel {
        val factory = ViewModelFactory.getInstance(this) // FIXME: replace with DI
        return ViewModelProviders.of(this, factory).get(MovieViewModel::class.java)
    }

    private fun createAdapter(): RecyclerViewAdapter {
        val imageLoader = AssetImageLoader()
        val adapter = RecyclerViewAdapter()
        val movieClickListener = object : MovieItem.MovieItemClickListener {
            override fun onMovieItemClicked(movieItem: MovieItem) {
                openMovieDetails(movieItem.movie)
            }
        }

        adapter.register(ItemType.MOVIE, object : RecyclerViewAdapter.LayoutIdFactory<MovieItem>(R.layout.movie_item) {
            override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<MovieItem> {
                return MovieItem.ViewHolder(convertView, imageLoader, movieClickListener)
            }
        })
        adapter.register(
            ItemType.FEEDBACK,
            object : RecyclerViewAdapter.LayoutIdFactory<FeedbackItem>(R.layout.feedback_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<FeedbackItem> {
                    return FeedbackItem.ViewHolder(convertView)
                }
            })
        adapter.register(
            ItemType.RATING,
            object : RecyclerViewAdapter.LayoutIdFactory<RatingItem>(R.layout.rating_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<RatingItem> {
                    return RatingItem.ViewHolder(convertView)
                }
            })
        adapter.register(
            ItemType.SURVEY,
            object : RecyclerViewAdapter.LayoutIdFactory<SurveyItem>(R.layout.survey_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<SurveyItem> {
                    return SurveyItem.ViewHolder(convertView)
                }
            })

        return adapter
    }

    //region Actions

    private fun openMovieDetails(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java)
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id)
        startActivity(intent)
    }

    private fun showStatistics() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun showProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    //endregion
}