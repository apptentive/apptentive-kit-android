package apptentive.com.android.movies

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import apptentive.com.android.movies.util.RecyclerViewAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = createAdapter()
        val layoutManager = GridLayoutManager(this, 2)

        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter

        val viewModel = ViewModelProviders.of(this).get(MovieViewModel::class.java)
        viewModel.movies.observe(this, Observer { movies ->
            adapter.setItems(movies.map { MovieItem(it) })
        })
    }

    private fun createAdapter(): RecyclerViewAdapter {
        val imageLoader = AssetImageLoader()
        val adapter = RecyclerViewAdapter()
        adapter.register(ItemType.MOVIE, object : RecyclerViewAdapter.LayoutIdFactory<MovieItem>(R.layout.movie_item) {
            override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<MovieItem> {
                return MovieItem.ViewHolder(convertView, imageLoader)
            }
        })

        return adapter
    }
}

private class AssetImageLoader : ImageLoader {
    override fun loadImage(path: String, imageView: ImageView) {
        val imageFile = "file:///android_asset/images$path"
        Picasso.get().load(imageFile).into(imageView)
    }
}