package apptentive.com.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

interface ViewHolderFactory {
    fun createItemView(parent: ViewGroup): View
    fun createViewHolder(itemView: View): ListViewAdapter.ViewHolder<*>
}

class LayoutViewHolderFactory(
    private val layoutId: Int,
    private val viewHolderCreator: (View) -> ListViewAdapter.ViewHolder<*>
) : ViewHolderFactory {
    override fun createItemView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    }

    override fun createViewHolder(itemView: View): ListViewAdapter.ViewHolder<*> {
        return viewHolderCreator.invoke(itemView)
    }
}