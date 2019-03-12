package apptentive.com.android.app

import android.view.View
import android.widget.ImageView
import android.widget.TextView

enum class ItemType {
    BEVERAGE,
    FEEDBACK
}

class BeverageItem(val id: String, val imageId: Int, val title: String) : Item(ItemType.BEVERAGE.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<BeverageItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.item_title)
        private val imageView: ImageView = view.findViewById(R.id.item_icon)

        override fun bindView(item: BeverageItem, position: Int) {
            titleView.text = item.title
            imageView.setImageResource(item.imageId)
        }
    }
}

class FeedbackItem(private val title: String) : Item(ItemType.FEEDBACK.ordinal) {
    class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<FeedbackItem>(view) {
        private val titleView: TextView = view.findViewById(R.id.title)

        override fun bindView(item: FeedbackItem, position: Int) {
            titleView.text = item.title
        }

    }

}