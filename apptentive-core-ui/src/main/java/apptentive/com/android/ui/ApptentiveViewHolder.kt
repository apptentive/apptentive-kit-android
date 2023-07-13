package apptentive.com.android.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
abstract class ApptentiveViewHolder<T : ListViewItem> constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    abstract fun bindView(item: T, position: Int)
    open fun updateView(item: T, position: Int, changeMask: Int) {
        bindView(item, position)
    }
}
