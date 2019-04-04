package apptentive.com.android.movies

import android.widget.ImageView
import com.squareup.picasso.Picasso

interface ImageLoader {
    fun loadImage(path: String, imageView: ImageView)
}

class AssetImageLoader : ImageLoader {
    override fun loadImage(path: String, imageView: ImageView) {
        val imageFile = "file:///android_asset/$path"
        Picasso.get().load(imageFile).into(imageView)
    }
}